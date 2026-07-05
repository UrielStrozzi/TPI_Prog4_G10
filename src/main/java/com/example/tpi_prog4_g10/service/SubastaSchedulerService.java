package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.enums.EstadoSubasta;
import com.example.tpi_prog4_g10.model.Puja;
import com.example.tpi_prog4_g10.model.Subasta;
import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.repository.PujaRepository;
import com.example.tpi_prog4_g10.repository.SubastaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SubastaSchedulerService {

    @Autowired
    private SubastaRepository subastaRepository;

    @Autowired
    private PujaRepository pujaRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private HistorialEstadoService historialEstadoService;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void procesarTransicionesAutomaticas() {
        Instant ahora = Instant.now();

        // ── PUBLICADA → ACTIVA ────────────────────────────────
        List<Subasta> porActivar = subastaRepository
                .findByEstadoAndFechaInicioLessThanEqual(EstadoSubasta.PUBLICADA, ahora);

        for (Subasta subasta : porActivar) {
            log.info("Activando subasta id={}", subasta.getId());
            historialEstadoService.registrar(
                    subasta,
                    EstadoSubasta.PUBLICADA,
                    EstadoSubasta.ACTIVA,
                    null, // automático — sin usuario responsable
                    "Inicio automático");
            subasta.setEstado(EstadoSubasta.ACTIVA);
            subastaRepository.save(subasta);
        }

        // ── ACTIVA → ADJUDICADA / FINALIZADA ─────────────────
        List<Subasta> porCerrar = subastaRepository
                .findByEstadoAndFechaFinLessThanEqual(EstadoSubasta.ACTIVA, ahora);

        for (Subasta subasta : porCerrar) {
            log.info("Cerrando subasta id={}", subasta.getId());

            Optional<Puja> mejorPuja = pujaRepository.findTopBySubastaOrderByMontoDesc(subasta);

            if (mejorPuja.isPresent()) {
                // ── Adjudicar ─────────────────────────────────
                Usuario ganador = mejorPuja.get().getUsuario();
                subasta.adjudicar(ganador);

                historialEstadoService.registrar(
                        subasta,
                        EstadoSubasta.ACTIVA,
                        EstadoSubasta.ADJUDICADA,
                        null,
                        "Cierre automático con pujas");

                subastaRepository.save(subasta);

                // Notificar ganador y vendedor
                notificacionService.notificarAdjudicacion(subasta);
                log.info("Subasta id={} adjudicada a {}", subasta.getId(), ganador.getUsername());

            } else {
                // ── Finalizar sin adjudicación ────────────────
                historialEstadoService.registrar(
                        subasta,
                        EstadoSubasta.ACTIVA,
                        EstadoSubasta.FINALIZADA,
                        null,
                        "Cierre automático sin pujas");

                subasta.setEstado(EstadoSubasta.FINALIZADA);
                subastaRepository.save(subasta);
                log.info("Subasta id={} finalizada sin pujas", subasta.getId());
            }
        }
    }
}
