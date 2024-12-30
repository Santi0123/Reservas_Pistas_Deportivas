package com.iesvdc.acceso.pistasdeportivas.controladores;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iesvdc.acceso.pistasdeportivas.modelos.Reserva;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoHorario;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoReserva;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoUsuario;

import io.micrometer.common.lang.NonNull;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



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

    // Nos va a mostrar las reservas 
    @GetMapping("")
    public String getReservas(Model model) {
        List<Reserva> reservas = repoReserva.findAll();
        model.addAttribute("reservas", reservas);
        return "reservas/reservas";
    }
    /**
     *  Agregar reservas
     */
    @GetMapping("/add")
    public String addReservas(Model model) {
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("operacion", "ADD");
        return "/reservas/add";
    }
    
    @PostMapping("/add")
    public String addReservas(@ModelAttribute("reserva") Reserva reserva) {
        repoReserva.save(reserva);
        return "redirect:/reservas";
    }

    /*
     * Sirve para editar a partir el id
     */
    @GetMapping("/edit/{id}")
    public String editReservas(@PathVariable @NonNull Long id, Model model) {
        Optional<Reserva> onReserva= repoReserva.findById(id);
        if(onReserva.isPresent()){
            model.addAttribute("reserva", onReserva.get());
            model.addAttribute("operacion", "EDIT");
            return "/reservas/add";
        }else{
            model.addAttribute("mensaje", "La instalación no exsiste");
            model.addAttribute("titulo", "Error editando instalación.");
            return "/error";
        }
    }

    @PostMapping("/edit/{id}")
    public String editReserva(@ModelAttribute("reserva") Reserva reserva) {
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
            model.addAttribute("borrando", "verdadero");
            model.addAttribute("operacion", "DEL");
            model.addAttribute("instalacion", onReserva.get());
            return "/resevas/add";
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
