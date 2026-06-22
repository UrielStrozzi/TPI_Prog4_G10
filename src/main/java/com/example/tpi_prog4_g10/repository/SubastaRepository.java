package com.example.tpi_prog4_g10.repository;

import com.example.tpi_prog4_g10.model.Subasta;       
import com.example.tpi_prog4_g10.enums.EstadoSubasta; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubastaRepository extends JpaRepository<Subasta, Long> {
    List<Subasta> findByEstado(EstadoSubasta estado);
}