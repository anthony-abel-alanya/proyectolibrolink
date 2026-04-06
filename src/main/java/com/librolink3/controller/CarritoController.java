package com.librolink3.controller;

import com.librolink3.model.Carrito;
import com.librolink3.model.Libro;
import com.librolink3.model.Usuario;
import com.librolink3.model.Actividad;
import com.librolink3.repository.ILibroRepository;
import com.librolink3.repository.IActividadRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CarritoController {

    @Autowired
    private ILibroRepository libroRepository;

    @Autowired
    private IActividadRepository actividadRepository;

    @GetMapping("/user/carrito")
    public String verCarrito(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            model.addAttribute("mensaje", "Debes iniciar sesión para ver tu carrito.");
            return "user/carrito";
        }

        List<Carrito> carrito = (List<Carrito>) session.getAttribute("carrito_" + usuario.getId());
        if (carrito == null) {
            carrito = new ArrayList<>();
        }

        double total = carrito.stream().mapToDouble(item -> item.getPrecio() * item.getCantidad()).sum();

        model.addAttribute("usuario", usuario);
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);

        return "user/carrito";
    }

    @GetMapping("/user/carrito/agregar/{id}")
    public String agregarAlCarrito(@PathVariable("id") Integer id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        Libro libro = libroRepository.findById(id).orElse(null);
        if (libro != null) {
            List<Carrito> carrito = (List<Carrito>) session.getAttribute("carrito_" + usuario.getId());
            if (carrito == null) {
                carrito = new ArrayList<>();
            }

            boolean existe = false;
            for (Carrito item : carrito) {
                if (item.getLibroId().equals(libro.getId())) {
                    item.setCantidad(item.getCantidad() + 1);
                    existe = true;
                    break;
                }
            }

            if (!existe) {
                Carrito nuevo = new Carrito();
                nuevo.setLibroId(libro.getId());
                nuevo.setTitulo(libro.getTitulo());
                nuevo.setPrecio(libro.getPrecio());
                nuevo.setCantidad(1);
                carrito.add(nuevo);
            }

            session.setAttribute("carrito_" + usuario.getId(), carrito);
        }
        return "redirect:/user/carrito";
    }

    @GetMapping("/user/carrito/eliminar/{id}")
    public String eliminarDelCarrito(@PathVariable("id") Integer id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario != null) {
            List<Carrito> carrito = (List<Carrito>) session.getAttribute("carrito_" + usuario.getId());
            if (carrito != null) {
                carrito.removeIf(item -> item.getLibroId().equals(id));
                session.setAttribute("carrito_" + usuario.getId(), carrito);
            }
        }
        return "redirect:/user/carrito";
    }

    @GetMapping("/user/carrito/confirmar")
    public String confirmarCompra(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            model.addAttribute("mensaje", "Debes iniciar sesión para confirmar la compra.");
            return "user/carrito";
        }

        List<Carrito> carrito = (List<Carrito>) session.getAttribute("carrito_" + usuario.getId());
        if (carrito == null || carrito.isEmpty()) {
            model.addAttribute("mensaje", "El carrito está vacío.");
            return "user/carrito";
        }

        // Registrar cada libro comprado en actividades
        for (Carrito item : carrito) {
            Libro libro = libroRepository.findById(item.getLibroId()).orElse(null);
            if (libro != null) {
                for (int i = 0; i < item.getCantidad(); i++) {
                    Actividad actividad = new Actividad();
                    actividad.setUsuario(usuario);
                    actividad.setLibro(libro);
                    actividad.setFecha(Date.valueOf(LocalDate.now())); 
                    actividad.setDescripcion("Compra de libro: " + libro.getTitulo());
                    actividadRepository.save(actividad);
                }
            }
        }

        session.removeAttribute("carrito_" + usuario.getId());
        model.addAttribute("usuario", usuario);
        model.addAttribute("mensaje", "¡Compra confirmada con éxito!");
        model.addAttribute("carrito", new ArrayList<Carrito>());
        model.addAttribute("total", 0);

        return "user/carrito";
    }
}

