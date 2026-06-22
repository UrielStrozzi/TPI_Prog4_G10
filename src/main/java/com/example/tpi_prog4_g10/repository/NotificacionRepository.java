package com.example.tpi_prog4_g10.repository;

import com.example.tpi_prog4_g10.model.Notificacion;
import com.example.tpi_prog4_g10.enums.EstadoNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioIdAndEstado(Long usuarioId, EstadoNotificacion estado);
}