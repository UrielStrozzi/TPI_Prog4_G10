package com.example.tpi_prog4_g10.repository;

import com.example.tpi_prog4_g10.model.Puja;
import com.example.tpi_prog4_g10.model.Subasta;
import com.example.tpi_prog4_g10.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PujaRepository extends JpaRepository<Puja, Long> {
    List<Puja> findBySubastaIdOrderByMontoDesc(Long subastaId);
    
    List<Puja> findByUsuarioId(Long usuarioId);

    List<Puja> findBySubastaOrderByFechaHoraDesc(Subasta subasta);

    boolean existsBySubastaId(Long subastaId);

    long countBySubasta(Subasta subasta);

    long countBySubastaId(Long subastaId);

    Optional<Puja> findTopBySubastaOrderByMontoDesc(Subasta subasta);

    List<Puja> findByUsuarioOrderByFechaHoraDesc(Usuario usuario);
}
