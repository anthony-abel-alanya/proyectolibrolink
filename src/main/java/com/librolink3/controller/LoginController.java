package com.librolink3.controller;

import com.librolink3.model.Usuario;
import com.librolink3.repository.IUsuarioRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

	@Autowired
	private IUsuarioRepository usuarioRepository;

	/*
	 * @GetMapping("/login") public String mostrarLogin() { return "login"; }
	 */

	@GetMapping("/login")
	public String mostrarLogin(@RequestParam(value = "logout", required = false) String logout, Model model) {
		
		if (logout != null) {
			model.addAttribute("mensaje", "Has cerrado sesión correctamente");
		}
		return "login";
	}

	// Procesar login
	@PostMapping("/login")
	public String login(@RequestParam String email, @RequestParam String password, Model model, HttpSession session) {

		Usuario usuario = usuarioRepository.findByEmail(email);

		if (usuario == null || !usuario.getPassword().equals(password)) {
			model.addAttribute("error", "Email o contraseña incorrectos");
			return "login";
		}

		// Guardar usuario en sesión
		session.setAttribute("usuarioLogueado", usuario);

		// Redirigir según rol
		if ("ROLE_ADMIN".equals(usuario.getRol().getNombre())) {
			return "redirect:/admin/libros";
		} else {
			return "redirect:/";
		}
	}

	// Logout manual
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login?logout";
	}
}
