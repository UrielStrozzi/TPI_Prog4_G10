package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.model.Subasta; 
import com.example.tpi_prog4_g10.enums.EstadoSubasta; 
import com.example.tpi_prog4_g10.repository.SubastaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubastaService {

    @Autowired
    private SubastaRepository subastaRepository;

    
    public Subasta crearSubasta(Subasta subasta) {
    
        if (subasta.getFechaCierre() == null || subasta.getFechaInicio() == null) {
            throw new RuntimeException("Error: Las fechas de inicio y cierre son obligatorias."); 
        }
        
        if (subasta.getFechaCierre().isBefore(subasta.getFechaInicio()) || 
            subasta.getFechaCierre().isEqual(subasta.getFechaInicio())) {
            throw new RuntimeException("Error: La fecha de cierre debe ser posterior a la fecha de inicio."); 
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