package com.iesvdc.acceso.pistasdeportivas.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iesvdc.acceso.pistasdeportivas.modelos.Reserva;
import com.iesvdc.acceso.pistasdeportivas.modelos.Usuario;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoReserva;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoUsuario;

@Controller
@RequestMapping("/mis-datos")
public class ControDatos {

    @Autowired
    RepoUsuario repoUsuario;

    @Autowired
    RepoReserva repoReserva;

    private Usuario getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return repoUsuario.findByUsername(
                authentication.getName()).get(0);
    }

    @GetMapping("")
    public String misDatos(Model modelo) {

        Usuario usuario = getLoggedUser();
        List<Reserva> listaReservas = repoReserva.findByUsuario(usuario);
        modelo.addAttribute("usuario", usuario);
        modelo.addAttribute("reservas", listaReservas);

        return "mis-datos/mis-datos";
    }
}
