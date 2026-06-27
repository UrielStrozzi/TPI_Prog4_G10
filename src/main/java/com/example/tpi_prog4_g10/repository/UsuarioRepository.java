package com.example.tpi_prog4_g10.repository;

import com.example.tpi_prog4_g10.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombre(String nombre); 
    Optional<Usuario> findByEmail(String email);
    Boolean existsByNombre(String nombre);         
    Boolean existsByEmail(String email);
    Optional<Usuario> findByUsername(String username);
}

