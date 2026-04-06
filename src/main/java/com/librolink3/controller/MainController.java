package com.librolink3.controller;

import com.librolink3.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario != null) {
            if ("ROLE_ADMIN".equals(usuario.getRol().getNombre())) {
                return "redirect:/admin/libros";
            } else {
                return "redirect:/user/catalogo";
            }
        }

        return "index"; 
    }
    @GetMapping("/nosotros")
    public String nosotros() {
        return "nosotros";
    }

}
