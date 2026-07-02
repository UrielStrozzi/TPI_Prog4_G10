package com.example.tpi_prog4_g10.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.tpi_prog4_g10.repository.*;
import com.example.tpi_prog4_g10.enums.EstadoSubasta;
import com.example.tpi_prog4_g10.model.Puja;
import com.example.tpi_prog4_g10.model.Subasta;
import com.example.tpi_prog4_g10.model.Usuario;

import jakarta.transaction.Transactional;

@Service
public class SubastaSchedulerService {

    @Autowired
    private SubastaRepository subastaRepository;

    @Autowired
    private PujaRepository pujaRepository;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void procesarTransicionesAutomaticas() {

        Instant ahora = Instant.now();

        // PUBLICADA -> ACTIVA
        List<Subasta> porActivar = subastaRepository.findByEstadoAndFechaInicioLessThanEqual(
                EstadoSubasta.PUBLICADA,
                ahora);

        for (Subasta subasta : porActivar) {
            subasta.setEstado(EstadoSubasta.ACTIVA);
            subastaRepository.save(subasta);
        }

        // ACTIVA -> ADJUDICADA o FINALIZADA
        List<Subasta> porCerrar = subastaRepository.findByEstadoAndFechaCierreLessThanEqual(
                EstadoSubasta.ACTIVA,
                ahora);
            for (Subasta subasta : porCerrar) {

                Optional<Puja> mejorPuja =
                        pujaRepository.findTopBySubastaOrderByMontoDesc(subasta);

                if (mejorPuja.isPresent()) {

                    Puja pujaGanadora = mejorPuja.get();
                    Usuario usuarioGanador = pujaGanadora.getUsuario();

                    subasta.adjudicar(usuarioGanador);

                } else {

                    subasta.setEstado(EstadoSubasta.FINALIZADA);
                }

                subastaRepository.save(subasta);
            }
    }
}
