package com.iesvdc.acceso.pistasdeportivas.controladores;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iesvdc.acceso.pistasdeportivas.modelos.Horario;
import com.iesvdc.acceso.pistasdeportivas.modelos.Instalacion;
import com.iesvdc.acceso.pistasdeportivas.modelos.Reserva;
import com.iesvdc.acceso.pistasdeportivas.modelos.Usuario;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoHorario;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoInstalacion;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoReserva;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoUsuario;

import lombok.experimental.PackagePrivate;

import org.springframework.web.bind.annotation.RequestParam;


@Controller
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/reservas")
public class ControReservas {
    
    /*  Nos permite hacer inyecciones 
     *  de dependencias de formas automaticas
     */

    //Lo necesitamos para las reservas
    @Autowired
    RepoReserva repoReserva;
    // Lo necesitamos para los usuarios
    @Autowired
    RepoUsuario repoUsuario;
    // Lo neecsitamos para la hora 
    @Autowired
    RepoHorario repoHorario;

    @Autowired
    RepoInstalacion repoInstalacion;

    // Nos va a mostrar las reservas 
    @GetMapping("")
    public String getReservas(
        Model model,
        @PageableDefault(size=10,sort="id")Pageable pageable) {
        
        Page<Reserva> page = repoReserva.findAll(pageable);
        model.addAttribute("page", page);
        model.addAttribute("reservas", page.getContent());
        model.addAttribute("usuarios", repoUsuario.findAll());
        return "reservas/reservas";
    }
    //para ver el usuario del maestro detalle
    @GetMapping("/usuario/{id}")
    public String getReservasByUsuario(
        @PathVariable @NonNull Long id,
        Model model,
        @PageableDefault(size=10, sort = "id")Pageable pageable) {
        
        Optional<Usuario> usuarioO = repoUsuario.findById(id); 

        if(usuarioO.isPresent()){
            Page<Reserva> page = repoReserva.findByUsuario(usuarioO.get(), pageable);
            model.addAttribute("page", page);
            model.addAttribute("reservas", page.getContent());
            model.addAttribute("usuarios", repoUsuario.findAll());
            model.addAttribute("usuario", usuarioO.get());
            return "reservas/reservas";
        }else{
            model.addAttribute("mensaje", "El usuario no existe");
            model.addAttribute("titulo", "No se encuentra el usuario");
            return "/error";
        }
    }
    


    /**
     *  Agregar reservas
     */
    @GetMapping("/add")
    public String addReserva(Model model) {
        model.addAttribute("usuarios", repoUsuario.findAll());
        model.addAttribute("localdate", LocalDate.now());
        return "/reservas/add";
    }


    @GetMapping("/add/usuario/{usuarioID}")
    public String getMethodName(
        @PathVariable Long usuarioID,
        @RequestParam String fecha,
        Model model) {

        Optional<Usuario> usuarioO = repoUsuario.findById(usuarioID);

        if(usuarioO.isPresent()){
            model.addAttribute("usuario", usuarioO.get());
            model.addAttribute("usuarios", repoUsuario.findAll());
            model.addAttribute("instalaciones", repoInstalacion.findAll());
            model.addAttribute("localdate", LocalDate.parse(fecha));
            return "reservas/add";
        }else{
            model.addAttribute("mensaje", "El usuario no existe");
            model.addAttribute("titulo", "No se encuentra el usuario");
            return "/error";
        }
    }

    @GetMapping("/add/usuario/{usuarioID}/instalacion/{instalacionID}")
    public String getMethodName(
        @PathVariable Long usuarioID,
        @PathVariable Long instalacionID,
        @RequestParam String fecha,
        Model model) {
        
        Optional<Usuario> usuarioO = repoUsuario.findById(usuarioID);
        Optional<Instalacion> instalacionO = repoInstalacion.findById(instalacionID);

        if(usuarioO.isPresent() && instalacionO.isPresent()){

            List<Horario> listaHorariosDisponibles = repoHorario.findHorariosDisponibles(instalacionO.get(), LocalDate.parse(fecha));
            
            model.addAttribute("usuario", usuarioO.get());
            model.addAttribute("usuarios", repoUsuario.findAll());
            model.addAttribute("instalacion", instalacionO.get());
            model.addAttribute("instalaciones", repoInstalacion.findAll());
            model.addAttribute("horarios", listaHorariosDisponibles);
            model.addAttribute("localdate", LocalDate.parse(fecha));
            return "/reservas/add";
        }else{
            model.addAttribute("mensaje", "El usuario no existe");
            model.addAttribute("titulo", "No se encuentra el usuario");
            return "/error";
        }
    }
    
    @GetMapping("/add/usuario/{usuarioID}/instalacion/{instalacionID}/horario/{horarioID}")
    public String getMethodName(
        @PathVariable Long usuarioID,
        @PathVariable Long instalacionID,
        @PathVariable Long horarioID,
        @RequestParam String fecha,
        Model model) {

        Optional<Usuario> usuarioO = repoUsuario.findById(usuarioID);
        Optional<Horario> horarioO = repoHorario.findById(horarioID);

        if(usuarioO.isPresent() && horarioO.isPresent()){

            // Se crea la reserva a partir de: 
            Reserva reserva = new Reserva();
            //Informacion del usuario
            Usuario usuario = usuarioO.get();
            // Informacion del horario
            Horario horario = horarioO.get();

            // Le ponemos el usuario
            reserva.setUsuario(usuario);
            // Le ponemos el horario
            reserva.setHorario(horario);
            // Le ponemos la fecha
            reserva.setFecha(LocalDate.parse(fecha));

            model.addAttribute("reserva", reserva);
            return "/reservas/confirmar-reserva";
        }else{
            model.addAttribute("mensaje", "El usuario no existe");
            model.addAttribute("titulo", "No se encuentra el usuario");
            return "/error";
        }
    }

    @PostMapping("/add/usuario/{usuarioID}/instalacion/{instalacionID}/horario/{horarioID}")
    public String addReservas(@ModelAttribute("reserva") Reserva reserva) {
        repoReserva.save(reserva);
        return "redirect:/reservas";
    }

    /**
     *  Borrar las reservas
     */
    @GetMapping("/del/{id}")
    public String delReserva(@PathVariable @NonNull Long id, Model model) {
        
        Optional<Reserva> onReserva = repoReserva.findById(id);
        if(onReserva.isPresent()){
            model.addAttribute("reserva", onReserva.get());
            return "/reservas/del";
        }else{
            model.addAttribute("mensaje", "La instalación no exsiste");
            model.addAttribute("titulo", "Error borrando instalación.");
            return "/error";
        }
    }

    @PostMapping("/del/{id}")
    public String delReserva(@ModelAttribute("reserva") Reserva reserva) {
        repoReserva.delete(reserva);
        return "redirect:/reservas";
    }
}
