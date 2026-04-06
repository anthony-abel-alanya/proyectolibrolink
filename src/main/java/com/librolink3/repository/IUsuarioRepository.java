package com.librolink3.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.librolink3.model.Usuario;

public interface IUsuarioRepository  extends JpaRepository<Usuario, Integer> {
	Usuario findByEmail(String email);
}
