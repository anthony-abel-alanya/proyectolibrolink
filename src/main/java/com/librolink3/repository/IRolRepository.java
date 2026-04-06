package com.librolink3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.librolink3.model.Rol;

public interface IRolRepository extends JpaRepository<Rol, Integer> {
    Rol findByNombre(String nombre);
}
