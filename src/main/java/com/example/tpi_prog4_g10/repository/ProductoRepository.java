package com.example.tpi_prog4_g10.repository;

import com.example.tpi_prog4_g10.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}