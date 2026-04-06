package com.librolink3.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.librolink3.model.Lectura;
import com.librolink3.model.Libro;
import com.librolink3.model.Usuario;

public interface ILecturaRepository  extends JpaRepository<Lectura, Integer>  {
	 
}
