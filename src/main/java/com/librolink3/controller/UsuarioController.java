package com.librolink3.controller;

import com.librolink3.model.Actividad;
import com.librolink3.model.Categoria;
import com.librolink3.model.Libro;
import com.librolink3.model.Usuario;
import com.librolink3.repository.IActividadRepository;
import com.librolink3.repository.ICategoriaRepository;
import com.librolink3.repository.ILibroRepository;
import com.librolink3.repository.ILecturaRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UsuarioController {

    @Autowired
    private ILibroRepository libroRepository;

    @Autowired
    private ILecturaRepository lecturaRepository;

    @Autowired
    private IActividadRepository actividadRepository;

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @GetMapping("/catalogo")
    public String verCatalogo(HttpSession session, Model model) {
        List<Libro> libros = libroRepository.findAll();
        List<Categoria> categorias = categoriaRepository.findAll();

        model.addAttribute("libros", libros);
        model.addAttribute("categorias", categorias);

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", usuario);

        return "user/catalogo";
    }

    @GetMapping("/catalogo/{id}")
    public String verDetalleLibro(@PathVariable Integer id, HttpSession session, Model model) {
        Libro libro = libroRepository.findById(id).orElse(null);
        if (libro == null) {
            return "redirect:/user/catalogo";
        }
        model.addAttribute("libro", libro);

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", usuario);

        return "user/detalle_libro";
    }

    @GetMapping("/catalogo/buscar")
    public String buscarLibros(@RequestParam(required = false) String autor,
                               @RequestParam(required = false) Integer categoriaId,
                               HttpSession session, Model model) {

        List<Libro> libros;

        if (autor != null && !autor.isBlank()) {
            libros = libroRepository.findByAutor(autor);
        } else if (categoriaId != null) {
            libros = libroRepository.findByCategoriaId(categoriaId);
        } else {
            libros = libroRepository.findAll();
        }

        model.addAttribute("categorias", categoriaRepository.findAll());

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", usuario);

        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("libros", libros);

        return "user/catalogo";
    }

    // Actividades del usuario
    @GetMapping("/actividades")
    public String verActividades(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", usuario);

        if (usuario == null) {
            model.addAttribute("mensaje", "Debes iniciar sesión para ver tus actividades.");
            return "user/actividades";
        }

        List<Actividad> actividades = actividadRepository.findByUsuario(usuario);
        model.addAttribute("actividades", actividades);

        double totalPrecio = actividades.stream()
                .filter(a -> a.getLibro() != null)
                .mapToDouble(a -> a.getLibro().getPrecio())
                .sum();
        model.addAttribute("totalPrecio", totalPrecio);

        return "user/actividades";
    }
}



