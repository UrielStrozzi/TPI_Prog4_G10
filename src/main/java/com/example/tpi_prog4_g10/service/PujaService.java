package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.model.Puja;
import com.example.tpi_prog4_g10.model.Subasta;
import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.model.VMisPujas;
import com.example.tpi_prog4_g10.enums.EstadoSubasta;
import com.example.tpi_prog4_g10.repository.PujaRepository;
import com.example.tpi_prog4_g10.repository.SubastaRepository;
import com.example.tpi_prog4_g10.repository.UsuarioRepository;
import com.example.tpi_prog4_g10.repository.VMisPujasRepository;

import jakarta.persistence.OptimisticLockException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class PujaService {

    @Autowired
    private PujaRepository pujaRepository;

    @Autowired
    private SubastaRepository subastaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VMisPujasRepository vMisPujasRepository;

    
    @Transactional
    public Puja registrarPuja(Long subastaId, Long usuarioId, BigDecimal montoOfrecido) {

        // Bloqueo pesimista — evita condición de carrera entre pujas simultáneas
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RuntimeException("Error: Subasta no encontrada."));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado."));

        // ── Validaciones ──────────────────────────────────────
        if (subasta.getEstado() != EstadoSubasta.ACTIVA) {
            throw new RuntimeException("Error: Solo se puede pujar en subastas ACTIVAS.");
        }

        if (Instant.now().isAfter(subasta.getFechaFin())) {
            throw new RuntimeException("Error: La subasta ya ha cerrado.");
        }

        if (usuario.isBloqueado()) {
            throw new RuntimeException("Error: Tu cuenta está bloqueada y no podés realizar pujas.");
        }

        if (subasta.getVendedor().getId().equals(usuario.getId())) {
            throw new RuntimeException("Error: El vendedor no puede pujar en su propia subasta.");
        }

        // ── Monto mínimo ──────────────────────────────────────
        BigDecimal montoMinimo = subasta.getMontoActual() == null
                ? subasta.getPrecioBase()
                : subasta.getMontoActual().add(subasta.getIncrementoMinimo());

        if (montoOfrecido.compareTo(montoMinimo) < 0) {
            throw new RuntimeException("La puja debe ser de al menos $" + montoMinimo);
        }

        // ── Registrar puja ────────────────────────────────────
        try {
            Puja nuevaPuja = Puja.builder()
                    .subasta(subasta)
                    .usuario(usuario)
                    .monto(montoOfrecido)
                    .build();

            pujaRepository.save(nuevaPuja);

            subasta.setMontoActual(montoOfrecido);
            subastaRepository.save(subasta); // ← acá actúa el @Version

            return nuevaPuja;

        } catch (OptimisticLockException e) {
            throw new RuntimeException("Otra puja fue registrada al mismo tiempo, intentá de nuevo");
        }
    }

    public List<VMisPujas> obtenerMisPujas(Long usuarioId) {
    return vMisPujasRepository.findByUsuarioId(usuarioId);
}
}