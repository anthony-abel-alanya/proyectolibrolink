package com.librolink3.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.librolink3.model.Actividad;
import com.librolink3.model.Libro;
import com.librolink3.model.Usuario;

public interface IActividadRepository  extends JpaRepository<Actividad, Integer> {
	List<Actividad> findByUsuario(Usuario usuario);
	boolean existsByLibro(Libro libro);
	
}
