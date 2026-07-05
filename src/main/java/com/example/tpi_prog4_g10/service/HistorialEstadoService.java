package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.enums.EstadoSubasta;
import com.example.tpi_prog4_g10.model.HistorialEstado;
import com.example.tpi_prog4_g10.model.Subasta;
import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.repository.HistorialEstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class HistorialEstadoService {

    @Autowired
    private HistorialEstadoRepository historialEstadoRepository;

    /**
     * Registra un cambio de estado.
     * 
     * @param subasta        la subasta que cambió
     * @param estadoAnterior estado previo (null si es el primero)
     * @param estadoNuevo    estado al que pasa
     * @param responsable    usuario que lo hizo (null si es automático)
     * @param motivo         razón del cambio (null si no aplica)
     */
    public void registrar(Subasta subasta, EstadoSubasta estadoAnterior,
            EstadoSubasta estadoNuevo, Usuario responsable, String motivo) {
        HistorialEstado h = HistorialEstado.builder()
                .subasta(subasta)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .usuarioResponsable(responsable)
                .motivo(motivo)
                .fecha(Instant.now())
                .build();
        historialEstadoRepository.save(h);
    }

    public List<HistorialEstado> obtenerPorSubasta(Long subastaId) {
        return historialEstadoRepository.findBySubastaIdOrderByIdDesc(subastaId);
    }
}
