package com.example.tpi_prog4_g10.repository;

import com.example.tpi_prog4_g10.model.Subasta;

import jakarta.persistence.LockModeType;

import com.example.tpi_prog4_g10.enums.EstadoSubasta; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubastaRepository extends JpaRepository<Subasta, Long> {

    List<Subasta> findByEstado(EstadoSubasta estado);

    List<Subasta> findByEstadoAndFechaInicioLessThanEqual(
            EstadoSubasta estado,
            Instant fecha);

    List<Subasta> findByEstadoAndFechaFinLessThanEqual(
            EstadoSubasta estado,
            Instant fecha);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT s FROM Subasta s WHERE s.id = :id")
        Optional<Subasta> findByIdWithLock(@Param("id") Long id);
}