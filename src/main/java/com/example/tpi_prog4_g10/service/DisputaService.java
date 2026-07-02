package com.example.tpi_prog4_g10.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tpi_prog4_g10.model.Disputa;
import com.example.tpi_prog4_g10.model.Subasta;
import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.repository.DisputaRepository;
import com.example.tpi_prog4_g10.repository.SubastaRepository;
import com.example.tpi_prog4_g10.repository.UsuarioRepository;
import com.example.tpi_prog4_g10.enums.*;

import jakarta.transaction.Transactional;

@Service
public class DisputaService {
    
    @Autowired
    private DisputaRepository disputaRepository;

    @Autowired
    private SubastaRepository subastaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Disputa abrirDisputa(Long subastaId, String username,
                                String motivo, String descripcion) {

        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RuntimeException("Subasta no encontrada."));

        // Solo se puede abrir disputa sobre subasta ADJUDICADA
        if (subasta.getEstado() != EstadoSubasta.ADJUDICADA) {
            throw new RuntimeException(
                    "Solo se puede abrir una disputa sobre subastas ADJUDICADAS.");
        }

        Usuario iniciador = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // Solo el vendedor o el ganador pueden abrir la disputa
        boolean esVendedor = subasta.getVendedor().getId().equals(iniciador.getId());
        boolean esGanador  = subasta.getGanador() != null &&
                             subasta.getGanador().getId().equals(iniciador.getId());

        if (!esVendedor && !esGanador) {
            throw new RuntimeException(
                    "Solo el vendedor o el ganador pueden abrir una disputa.");
        }

        // Cambiar estado de la subasta a EN_DISPUTA
        subasta.setEstado(EstadoSubasta.EN_DISPUTA);
        subastaRepository.save(subasta);

        // Crear la disputa
        Disputa disputa = Disputa.builder()
                .subasta(subasta)
                .reclamante(iniciador)
                .motivo(motivo)
                .descripcion(descripcion)
                .estado(EstadoDisputa.ABIERTA)
                .fechaApertura(Instant.now())
                .build();

        return disputaRepository.save(disputa);
    }

    @Transactional
    public Disputa resolverDisputa(Long disputaId, String adminUsername,
                                   String resolucion, String estadoFinalStr) {

        Disputa disputa = disputaRepository.findById(disputaId)
                .orElseThrow(() -> new RuntimeException("Disputa no encontrada."));

        if (disputa.getEstado() != EstadoDisputa.ABIERTA) {
            throw new RuntimeException("La disputa ya fue resuelta.");
        }

        // busca por email o username
        Usuario admin = usuarioRepository.findByEmail(adminUsername)
        .or(() -> usuarioRepository.findByUsername(adminUsername))
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // Resolver la disputa
        disputa.setResolucion(resolucion);
        disputa.setResueltoPor(admin);
        disputa.setEstado(EstadoDisputa.RESUELTA);
        disputa.setFechaResolucion(Instant.now());

        // Aplicar estado final a la subasta
        EstadoSubasta estadoFinal = EstadoSubasta.valueOf(estadoFinalStr);
        if (estadoFinal != EstadoSubasta.ADJUDICADA &&
            estadoFinal != EstadoSubasta.FINALIZADA &&
            estadoFinal != EstadoSubasta.CANCELADA) {
            throw new RuntimeException(
                    "Estado final inválido. Usar: ADJUDICADA, FINALIZADA o CANCELADA.");
        }

        disputa.getSubasta().setEstado(estadoFinal);
        subastaRepository.save(disputa.getSubasta());

        return disputaRepository.save(disputa);
    }

    public Disputa obtenerPorId(Long id) {
        return disputaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disputa no encontrada."));
    }
}
