package com.librolink3.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.librolink3.model.Categoria;
import com.librolink3.model.Libro;

@Repository
public interface ILibroRepository extends JpaRepository<Libro, Integer> {
	List<Libro> findByAutor(String autor);
	List<Libro> findByCategoriaId(Integer categoriaId);
	boolean existsByCategoria(Categoria categoria);
}
