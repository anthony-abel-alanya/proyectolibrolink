package com.librolink3.controller;

import com.librolink3.model.Rol;
import com.librolink3.model.Usuario;
import com.librolink3.repository.IRolRepository;
import com.librolink3.repository.IUsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RegistroController {

	@Autowired
	private IUsuarioRepository usuarioRepository;

	@Autowired
	private IRolRepository rolRepository;

	// Mostrar el formulario de registro
	@GetMapping("/registro")
	public String mostrarFormularioRegistro() {
		return "registro";
	}

	// Manejar el registro de usuario con inicio de sesión automático
	@PostMapping("/registro")
	public String registrarUsuario(@RequestParam String nombre, @RequestParam String email,
			@RequestParam String password, HttpSession session) { 

		if (usuarioRepository.findByEmail(email) != null) {
			return "redirect:/registro?error=Usuario ya existe";
		}

		// Guardar sin bcrypt
		String plainPassword = password;

		Rol rol;
		if (usuarioRepository.count() == 0) {
			rol = rolRepository.findByNombre("ROLE_ADMIN");
		} else {
			rol = rolRepository.findByNombre("ROLE_USER");
		}

		if (rol == null) {
			return "redirect:/registro?error=Rol no encontrado";
		}

		Usuario nuevoUsuario = new Usuario();
		nuevoUsuario.setNombre(nombre);
		nuevoUsuario.setEmail(email);
		nuevoUsuario.setPassword(plainPassword);
		nuevoUsuario.setRol(rol);

		usuarioRepository.save(nuevoUsuario);

		// Iniciar sesión automáticamente
		session.setAttribute("usuarioLogueado", nuevoUsuario);

		// Redirigir según rol
		if ("ROLE_ADMIN".equals(nuevoUsuario.getRol().getNombre())) {
			return "redirect:/admin/libros";
		} else {
			return "redirect:/user/catalogo";
		}
	}

}
