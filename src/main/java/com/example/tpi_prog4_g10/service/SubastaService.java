package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.model.Subasta;
import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.enums.EstadoSubasta;
import com.example.tpi_prog4_g10.repository.SubastaRepository;
import com.example.tpi_prog4_g10.repository.PujaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.tpi_prog4_g10.repository.UsuarioRepository;

import java.time.Instant;
import java.util.List;

@Service
public class SubastaService {

    @Autowired
    private SubastaRepository subastaRepository;

    @Autowired
    private PujaRepository pujaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    
    public Subasta crearSubasta(Subasta subasta) {

        if (subasta.getProducto() == null) {
            throw new RuntimeException("La subasta debe tener un producto.");
        }

        if (subasta.getVendedor() == null) {
            subasta.setVendedor(subasta.getProducto().getVendedor());
        }

        if (subasta.getFechaFin() == null
                || subasta.getFechaInicio() == null) {
            throw new RuntimeException(
                    "Las fechas de inicio y fin son obligatorias.");
        }

        if (!subasta.getFechaFin()
                .isAfter(subasta.getFechaInicio())) {
            throw new RuntimeException(
                    "La fecha de cierre debe ser posterior a la fecha de inicio.");
        }

        subasta.setEstado(EstadoSubasta.BORRADOR);

        return subastaRepository.save(subasta);
    }

    public Subasta publicarSubasta(Long id) {
        Subasta subasta = obtenerPorId(id);
        
        if (subasta.getEstado() != EstadoSubasta.BORRADOR) {
            throw new RuntimeException("Error: Solo se pueden publicar subastas en estado BORRADOR.");
        }
        
        subasta.setEstado(EstadoSubasta.PUBLICADA); 
        return subastaRepository.save(subasta);
    }

    public Subasta cancelarSubasta(Long id, String motivo, String emailSolicitante, boolean esAdmin) {
        Subasta subasta = subastaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Subasta no encontrada: " + id));

        EstadoSubasta estadoActual = subasta.getEstado();

        // ── CASO 1: PUBLICADA → CANCELADA ─────────────────────────────────────
        if (estadoActual == EstadoSubasta.PUBLICADA) {

            if (esAdmin) {
                // ADMIN siempre puede cancelar una PUBLICADA
                subasta.setEstado(EstadoSubasta.CANCELADA);
                subasta.setMotivoCancelacion(motivo);

            } else {
                // SELLER solo puede si es dueño y no tiene pujas
                verificarDueno(subasta, emailSolicitante);

                boolean tienePujas = pujaRepository.existsBySubastaId(id);
                if (tienePujas) {
                    throw new RuntimeException("No podés cancelar una subasta que ya tiene pujas");
                }

                subasta.setEstado(EstadoSubasta.CANCELADA);
                subasta.setMotivoCancelacion(motivo);
            }

        // ── CASO 2: ACTIVA → CANCELADA ────────────────────────────────────────
        } else if (estadoActual == EstadoSubasta.ACTIVA) {

            if (!esAdmin) {
                throw new RuntimeException("Solo un ADMIN puede cancelar una subasta ACTIVA");
            }

            if (motivo == null || motivo.isBlank()) {
                throw new RuntimeException("El motivo es obligatorio para cancelar una subasta ACTIVA");
            }

            subasta.setEstado(EstadoSubasta.CANCELADA);
            subasta.setMotivoCancelacion(motivo);

        // ── ESTADO INVÁLIDO ───────────────────────────────────────────────────
        } else {
            throw new RuntimeException(
                "No se puede cancelar una subasta en estado: " + estadoActual
            );
        }

        subasta.setFechaCancelacion(Instant.now());

        Usuario solicitante = usuarioRepository.findByEmail(emailSolicitante)
            .or(() -> usuarioRepository.findByUsername(emailSolicitante))
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        subasta.setCanceladoPor(solicitante);

        return subastaRepository.save(subasta);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private void verificarDueno(Subasta subasta, String emailSolicitante) {
        if (!subasta.getVendedor().getEmail().equals(emailSolicitante)) {
            throw new RuntimeException("No tenés permiso para cancelar esta subasta");
        }
    }

    public List<Subasta> obtenerTodas() {
        return subastaRepository.findAll();
    }

    public List<Subasta> obtenerPorEstado(EstadoSubasta estado) {
        return subastaRepository.findByEstado(estado);
    }

    public Subasta obtenerPorId(Long id) {
        return subastaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Subasta no encontrada. ID: " + id));
    }
}