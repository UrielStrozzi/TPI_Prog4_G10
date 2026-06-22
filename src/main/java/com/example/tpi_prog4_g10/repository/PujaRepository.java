package com.example.tpi_prog4_g10.repository;

import com.example.tpi_prog4_g10.model.Puja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PujaRepository extends JpaRepository<Puja, Long> {
    List<Puja> findBySubastaIdOrderByMontoDesc(Long subastaId);
    List<Puja> findByUsuarioId(Long usuarioId);
}
