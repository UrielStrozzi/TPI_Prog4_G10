package com.example.tpi_prog4_g10.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.tpi_prog4_g10.model.VMisPujas;;

@Repository
public interface VMisPujasRepository extends JpaRepository<VMisPujas, Long> {
    List<VMisPujas> findByUsuarioId(Long usuarioId);
    List<VMisPujas> findBySubastaId(Long subastaId);
}
