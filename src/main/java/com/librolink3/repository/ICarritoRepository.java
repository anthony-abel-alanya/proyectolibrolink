package com.librolink3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.librolink3.model.Carrito;

public interface ICarritoRepository extends JpaRepository<Carrito, Integer> {
}
