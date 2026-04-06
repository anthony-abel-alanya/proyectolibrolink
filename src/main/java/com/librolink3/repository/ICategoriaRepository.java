package com.librolink3.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.librolink3.model.Categoria;

public interface ICategoriaRepository extends JpaRepository<Categoria, Integer> {
	//Categoria findByNombre(String nombre);
}
