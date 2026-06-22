package com.example.tpi_prog4_g10.repository;

import com.example.tpi_prog4_g10.model.Rol;
import com.example.tpi_prog4_g10.enums.NombreRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(NombreRol nombre);
}