package com.example.tpi_prog4_g10.repository;

import com.example.tpi_prog4_g10.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    List<HistorialEstado> findBySubastaIdOrderByIdDesc(Long subastaId);
}