package com.librolink3.controller;

import com.librolink3.model.*;
import com.librolink3.repository.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.List;


import javax.sql.DataSource;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private ILibroRepository libroRepository;

	@Autowired
	private IUsuarioRepository usuarioRepository;

	@Autowired
	private IActividadRepository actividadRepository;

	@Autowired
	private ICategoriaRepository categoriaRepository;

	/** Verifica si el usuario logueado es admin */
	private boolean esAdmin(HttpSession session) {
		Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
		return usuario != null && usuario.getRol() != null && "ROLE_ADMIN".equals(usuario.getRol().getNombre());
	}

	/** Gestionar Libros */
	@GetMapping("/libros")
	public String verLibrosAdmin(Model model, HttpSession session) {
		if (!esAdmin(session))
			return "login";

		List<Libro> libros = libroRepository.findAll();
		model.addAttribute("libros", libros);

		// Agregar usuario al modelo
		Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
		model.addAttribute("usuario", usuario);

		return "admin/gestion_libros";
	}

	/** Formulario para nuevo libro */
	@GetMapping("/libros/nuevo")
	public String mostrarFormularioNuevoLibro(Model model, HttpSession session) {
		if (!esAdmin(session))
			return "login";

		model.addAttribute("categorias", categoriaRepository.findAll());

		// Agregar usuario al modelo
		Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
		model.addAttribute("usuario", usuario);

		return "admin/nuevo_libro";
	}

	/** Guardar nuevo libro */
	@PostMapping("/libros/agregar")
	public String agregarLibro(@RequestParam String titulo, @RequestParam String autor, @RequestParam Integer categoria,
			@RequestParam String descripcion, @RequestParam Double precio, @RequestParam Integer stock,
			@RequestParam("imagen") MultipartFile imagen, Model model, HttpSession session) throws IOException {

		if (!esAdmin(session))
			return "login";

		Categoria cat = categoriaRepository.findById(categoria).orElse(null);
		if (cat == null) {
			model.addAttribute("mensaje", "Categoría no encontrada.");
			model.addAttribute("categorias", categoriaRepository.findAll());

			Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
			model.addAttribute("usuario", usuario);

			return "admin/nuevo_libro";
		}

		Libro libro = new Libro();
		libro.setTitulo(titulo);
		libro.setAutor(autor);
		libro.setDescripcion(descripcion);
		libro.setCategoria(cat);
		libro.setPrecio(precio);
		libro.setStock(stock);

		
		if (imagen != null && !imagen.isEmpty()) {
			String rutaCarpeta = "uploads/img/";
			Path carpetaPath = Paths.get(rutaCarpeta);
			if (!Files.exists(carpetaPath)) {
				Files.createDirectories(carpetaPath);
			}

			String nombreArchivo = imagen.getOriginalFilename(); 
			Path rutaArchivo = carpetaPath.resolve(nombreArchivo);
			Files.copy(imagen.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

			libro.setImagen(nombreArchivo);
		}

		libroRepository.save(libro);
		return "redirect:/admin/libros";
	}

	/** Formulario para editar libro */
	@GetMapping("/libros/{id}/editar")
	public String mostrarFormularioEditar(@PathVariable Integer id, Model model, HttpSession session) {
		if (!esAdmin(session))
			return "login";

		Libro libro = libroRepository.findById(id).orElse(null);
		if (libro == null)
			return "redirect:/admin/libros";

		List<Categoria> categorias = categoriaRepository.findAll();
		model.addAttribute("libro", libro);
		model.addAttribute("categorias", categorias);

		// Agregar usuario al modelo
		Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
		model.addAttribute("usuario", usuario);

		return "admin/editar_libro";
	}

	/** Guardar edición de libro */
	@PostMapping("/libros/{id}/editar")
	public String editarLibro(@PathVariable Integer id, @RequestParam String titulo, @RequestParam String autor,
			@RequestParam String descripcion, @RequestParam Integer categoria, @RequestParam Double precio,
			@RequestParam Integer stock, @RequestParam("imagen") MultipartFile imagen, HttpSession session)
			throws IOException {

		if (!esAdmin(session))
			return "login";

		Libro libro = libroRepository.findById(id).orElse(null);
		if (libro != null) {
			libro.setTitulo(titulo);
			libro.setAutor(autor);
			libro.setDescripcion(descripcion);
			libro.setPrecio(precio);
			libro.setStock(stock);

			Categoria cat = categoriaRepository.findById(categoria).orElse(null);
			if (cat != null) {
				libro.setCategoria(cat);
			}

			
			if (imagen != null && !imagen.isEmpty()) {
				String rutaCarpeta = "uploads/img/";
				Path carpetaPath = Paths.get(rutaCarpeta);
				if (!Files.exists(carpetaPath)) {
					Files.createDirectories(carpetaPath);
				}

				String nombreArchivo = imagen.getOriginalFilename(); // nombre original
				Path rutaArchivo = carpetaPath.resolve(nombreArchivo);
				Files.copy(imagen.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

				libro.setImagen(nombreArchivo);
			}

			libroRepository.save(libro);
		}

		return "redirect:/admin/libros";
	}

	/** Eliminar libro */
	/*@GetMapping("/libros/{id}/eliminar")
	public String eliminarLibro(@PathVariable Integer id, HttpSession session) {
		if (!esAdmin(session))
			return "login";

		libroRepository.deleteById(id);
		return "redirect:/admin/libros";
	}*/
	
	@GetMapping("/libros/{id}/eliminar")
	public String eliminarLibro(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
	    if (!esAdmin(session)) return "login";

	    Libro libro = libroRepository.findById(id).orElse(null);
	    if (libro == null) {
	        redirectAttributes.addFlashAttribute("error", "El libro no existe.");
	        return "redirect:/admin/libros";
	    }

	    // Verificar si el libro tiene actividades asociadas
	    if (actividadRepository.existsByLibro(libro)) {
	        redirectAttributes.addFlashAttribute("error",
	            "No se puede eliminar el libro '" + libro.getTitulo() + "' porque tiene actividades asociadas.");
	        return "redirect:/admin/libros";
	    }

	    libroRepository.delete(libro);
	    redirectAttributes.addFlashAttribute("success", "Libro eliminado correctamente.");
	    return "redirect:/admin/libros";
	}

	/** Ver usuarios */
	@GetMapping("/usuarios")
	public String verUsuariosAdmin(@RequestParam(value = "busqueda", required = false) String busqueda,
			@RequestParam(value = "rol", required = false) String rol, Model model, HttpSession session) {

		if (!esAdmin(session))
			return "login";

		List<Usuario> usuarios = usuarioRepository.findAll();

		if (busqueda != null && !busqueda.isBlank()) {
			String busqLower = busqueda.toLowerCase();
			usuarios = usuarios.stream().filter(u -> u.getNombre().toLowerCase().contains(busqLower)
					|| u.getEmail().toLowerCase().contains(busqLower)).toList();
		}

		if (rol != null && !rol.isBlank()) {
			usuarios = usuarios.stream().filter(u -> u.getRol() != null && u.getRol().getNombre().equalsIgnoreCase(rol))
					.toList();
		}

		model.addAttribute("usuarios", usuarios);

		// Agregar usuario al modelo
		Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
		model.addAttribute("usuario", usuario);

		return "admin/gestion_usuarios";
	}

	/** Ver actividades */
	@GetMapping("/actividades")
	public String verActividadesAdmin(Model model, HttpSession session) {
		if (!esAdmin(session))
			return "login";

		List<Actividad> actividades = actividadRepository.findAll();

		double totalPrecio = actividades.stream().filter(a -> a.getLibro() != null && a.getLibro().getPrecio() != null)
				.mapToDouble(a -> a.getLibro().getPrecio()).sum();

		model.addAttribute("actividades", actividades);
		model.addAttribute("totalPrecio", totalPrecio);

		// Agregar usuario al modelo
		Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
		model.addAttribute("usuario", usuario);

		return "admin/gestion_actividades";
	}
	
	
	
	// Ver todas las categorías
	@GetMapping("/categorias")
	public String verCategorias(Model model, HttpSession session) {
	    if (!esAdmin(session)) return "login";

	    List<Categoria> categorias = categoriaRepository.findAll();
	    model.addAttribute("categorias", categorias);

	    Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
	    model.addAttribute("usuario", usuario);

	    return "admin/gestion_categorias";
	}

	// Mostrar formulario para nueva categoría
	@GetMapping("/categorias/nuevo")
	public String mostrarFormularioNuevaCategoria(Model model, HttpSession session) {
	    if (!esAdmin(session)) return "login";

	    model.addAttribute("categoria", new Categoria());

	    Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
	    model.addAttribute("usuario", usuario);

	    return "admin/nueva_categoria";
	}

	// Guardar nueva categoría
	@PostMapping("/categorias/agregar")
	public String agregarCategoria(@ModelAttribute Categoria categoria, HttpSession session) {
	    if (!esAdmin(session)) return "login";

	    categoriaRepository.save(categoria);
	    return "redirect:/admin/categorias";
	}

	// Mostrar formulario para editar categoría
	@GetMapping("/categorias/{id}/editar")
	public String mostrarFormularioEditarCategoria(@PathVariable Integer id, Model model, HttpSession session) {
	    if (!esAdmin(session)) return "login";

	    Categoria categoria = categoriaRepository.findById(id).orElse(null);
	    if (categoria == null) return "redirect:/admin/categorias";

	    model.addAttribute("categoria", categoria);

	    Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
	    model.addAttribute("usuario", usuario);

	    return "admin/editar_categoria";
	}

	// Guardar edición de categoría
	@PostMapping("/categorias/{id}/editar")
	public String editarCategoria(@PathVariable Integer id, @ModelAttribute Categoria categoria, HttpSession session) {
	    if (!esAdmin(session)) return "login";

	    Categoria cat = categoriaRepository.findById(id).orElse(null);
	    if (cat != null) {
	        cat.setNombre(categoria.getNombre());
	        categoriaRepository.save(cat);
	    }

	    return "redirect:/admin/categorias";
	}

	// Eliminar categoría
	/*@GetMapping("/categorias/{id}/eliminar")
	public String eliminarCategoria(@PathVariable Integer id, HttpSession session) {
	    if (!esAdmin(session)) return "login";

	    categoriaRepository.deleteById(id);
	    return "redirect:/admin/categorias";
	}*/
	
	@GetMapping("/categorias/{id}/eliminar")
	public String eliminarCategoria(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
	    if (!esAdmin(session)) return "login";

	    Categoria categoria = categoriaRepository.findById(id).orElse(null);
	    if (categoria == null) {
	        redirectAttributes.addFlashAttribute("error", "La categoría no existe.");
	        return "redirect:/admin/categorias";
	    }

	    // Verificar si hay libros asociados
	    if (libroRepository.existsByCategoria(categoria)) {
	        redirectAttributes.addFlashAttribute("error",
	            "No se puede eliminar la categoría '" + categoria.getNombre() + "' porque tiene libros asociados.");
	        return "redirect:/admin/categorias";
	    }

	    categoriaRepository.delete(categoria);
	    redirectAttributes.addFlashAttribute("success", "Categoría eliminada correctamente.");
	    return "redirect:/admin/categorias";
	}
	
	
	@Autowired
	private DataSource dataSource; // javax.sql

	@Autowired
	private ResourceLoader resourceLoader; // core.io

	@GetMapping("/reportes")
	public void reportes(HttpServletResponse response) {
	    // opción 1
	    //response.setHeader("Content-Disposition", "attachment; filename=\"reporte.pdf\";");
	    // opción 2
	    response.setHeader("Content-Disposition", "inline;");
	    
	    response.setContentType("application/pdf");
	    try {
	        String ru = resourceLoader.getResource("classpath:static/reporteGeneral.jasper").getURI().getPath();
	        JasperPrint jasperPrint = JasperFillManager.fillReport(ru, null, dataSource.getConnection());
	        OutputStream outStream = response.getOutputStream();
	        JasperExportManager.exportReportToPdfStream(jasperPrint, outStream);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
