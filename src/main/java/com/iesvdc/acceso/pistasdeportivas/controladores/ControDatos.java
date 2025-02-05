package com.iesvdc.acceso.pistasdeportivas.controladores;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.iesvdc.acceso.pistasdeportivas.modelos.Horario;
import com.iesvdc.acceso.pistasdeportivas.modelos.Instalacion;
import com.iesvdc.acceso.pistasdeportivas.modelos.Reserva;
import com.iesvdc.acceso.pistasdeportivas.modelos.Usuario;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoHorario;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoInstalacion;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoReserva;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoUsuario;

@Controller
@RequestMapping("/mis-datos")
public class ControDatos {

    @Autowired
    RepoUsuario repoUsuario;

    @Autowired
    RepoReserva repoReserva;

    @Autowired
    RepoHorario repoHorario;

    @Autowired
    RepoInstalacion repoInstalacion;

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

    @GetMapping("/add")
    public String addReserva(Model model) {
        Usuario usuario = getLoggedUser();
        model.addAttribute("usuario", usuario);
        model.addAttribute("instalaciones", repoInstalacion.findAll());
        model.addAttribute("localdate", LocalDate.now());
        return "mis-datos/add";
    }

    @GetMapping("/add/usuario/{usuarioID}/instalacion/{instalacionID}")
    public String seleccionarInstalacion(@PathVariable Long instalacionID, @RequestParam String fecha, Model model) {
        Usuario usuario = getLoggedUser();
        Optional<Usuario> usuarioO = repoUsuario.findById(usuario.getId());
        Optional<Instalacion> instalacionO = repoInstalacion.findById(instalacionID);

        if (usuarioO.isPresent() && instalacionO.isPresent()) {
            List<Horario> listaHorariosDisponibles = repoHorario.findHorariosDisponibles(instalacionO.get(), LocalDate.parse(fecha));
            model.addAttribute("usuario", usuarioO.get());
            model.addAttribute("instalacion", instalacionO.get());
            model.addAttribute("instalaciones", repoInstalacion.findAll());
            model.addAttribute("horarios", listaHorariosDisponibles);
            model.addAttribute("localdate", LocalDate.parse(fecha));
            return "/mis-datos/add";
        } else {
            model.addAttribute("mensaje", "La instalación no existe");
            model.addAttribute("titulo", "Error seleccionando instalación");
            return "/error";
        }
    }

    @GetMapping("/add/usuario/{usuarioID}/instalacion/{instalacionID}/horario/{horarioID}")
    public String confirmarReserva(@PathVariable Long instalacionID, @PathVariable Long horarioID, @RequestParam String fecha, Model model) {
        Usuario usuario = getLoggedUser();
        Optional<Usuario> usuarioO = repoUsuario.findById(usuario.getId());
        Optional<Horario> horarioO = repoHorario.findById(horarioID);

        if (usuarioO.isPresent() && horarioO.isPresent()) {
            Reserva reserva = new Reserva();
            reserva.setUsuario(usuarioO.get());
            reserva.setHorario(horarioO.get());
            reserva.setFecha(LocalDate.parse(fecha));

            model.addAttribute("usuario", usuario);
            model.addAttribute("reserva", reserva);
            return "/mis-datos/confirmar-reserva";
        } else {
            model.addAttribute("mensaje", "El horario no existe");
            model.addAttribute("titulo", "Error seleccionando horario");
            return "/error";
        }
    }

    @PostMapping("/add/usuario/{usuarioID}/instalacion/{instalacionID}/horario/{horarioID}")
    public String addReservas(@ModelAttribute("reserva") Reserva reserva) {
        if (reserva.getFecha().isAfter(LocalDate.now().minusDays(1)) &&
            reserva.getFecha().isBefore(LocalDate.now().plusWeeks(1))) {
            repoReserva.save(reserva);
        }
        return "redirect:/mis-datos";
    }

    @GetMapping("/del/{id}")
    public String delReserva(@PathVariable Long id, Model model) {
        Usuario usuario = getLoggedUser();
        Optional<Reserva> onReserva = repoReserva.findById(id);
        if (onReserva.isPresent() && onReserva.get().getUsuario().equals(usuario)) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("reserva", onReserva.get());
            return "mis-datos/del";
        } else {
            model.addAttribute("mensaje", "No tienes permiso para eliminar esta reserva");
            model.addAttribute("titulo", "Error eliminando reserva");
            return "/error";
        }
    }

    @PostMapping("/del/{id}")
    public String delReservaConfirm(@PathVariable Long id) {
        Usuario usuario = getLoggedUser();
        Optional<Reserva> onReserva = repoReserva.findById(id);
        if (onReserva.isPresent() && onReserva.get().getUsuario().equals(usuario)) {
            repoReserva.delete(onReserva.get());
        }
        return "redirect:/mis-datos";
    }
}
