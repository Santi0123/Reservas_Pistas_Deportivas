package com.iesvdc.acceso.pistasdeportivas.controladores;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iesvdc.acceso.pistasdeportivas.modelos.Rol;
import com.iesvdc.acceso.pistasdeportivas.modelos.Usuario;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoUsuario;

import io.micrometer.common.lang.NonNull;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;



@Controller
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/usuario")
public class ControUsuarios {
    
    @Autowired
    RepoUsuario repoUsuario;
    
    @GetMapping("")
    public String getUsuarios(Model model) {
        List<Usuario> usuarios = repoUsuario.findAll();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", Rol.values());
    
        return "usuarios/usuarios";
    }
    

    @GetMapping("/add")
    public String addUsuarios(Model model) {
        Usuario usuario = new Usuario();
        usuario.setEnabled(false);
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Rol.values());
        model.addAttribute("operacion", "ADD");
        return "/usuarios/add";
    }
    
    @PostMapping("/add")
    public String addUsuarios(@ModelAttribute("usuario") Usuario usuario) {
        usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
        repoUsuario.save(usuario);
        return "redirect:/usuario";
    }
    
    @GetMapping("/edit/{id}")
    public String editUsuarios(@PathVariable @NonNull Long id, Model model) {
        Optional<Usuario> onUsuario = repoUsuario.findById(id);
        if (onUsuario.isPresent()) {
            model.addAttribute("usuario", onUsuario.get());
            model.addAttribute("roles", Rol.values());
            model.addAttribute("operacion", "EDIT");
            return "/usuarios/add";
        }else{
            model.addAttribute("mensaje", "El usuario no existe");
            model.addAttribute("titulo", "Error editando el usuario.");
            return "/error";
        }
    }
    
    @PostMapping("/edit/{id}")
    public String editUsuarios(@ModelAttribute("usuario") Usuario usuario) {
        if (usuario.getPassword().length() > 4) {
            usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
        }else{
            Usuario usuarioOld = repoUsuario.findById(usuario.getId()).get();
            usuario.setPassword(usuarioOld.getPassword());
        }

        repoUsuario.save(usuario);
        return "redirect:/usuario";
    }

    @GetMapping("/del/{id}")
    public String delUsuario(@PathVariable @NonNull Long id, Model model) {
        Optional<Usuario> onUsuario = repoUsuario.findById(id);
        if (onUsuario.isPresent()) {
            model.addAttribute("borrando", "verdadero");
            model.addAttribute("operacion", "DEL");
            model.addAttribute("usuario", onUsuario.get());
            model.addAttribute("roles", onUsuario.get().getTipo());
            return "/usuarios/add";
        }else{
            model.addAttribute("mensaje", "El usuario no existe");
            model.addAttribute("titulo", "Error borrarndo el usuario.");
            return "/error";
        }
    }
    

    @PostMapping("/del/{id}")
    public String delUsuario(@ModelAttribute("usuario") Usuario usuario) {
        repoUsuario.delete(usuario);
        return "redirect:/usuario";
    }
    

}
