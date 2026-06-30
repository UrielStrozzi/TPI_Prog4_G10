package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.model.Puja;
import com.example.tpi_prog4_g10.model.Subasta;
import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.enums.EstadoSubasta;
import com.example.tpi_prog4_g10.repository.PujaRepository;
import com.example.tpi_prog4_g10.repository.SubastaRepository;
import com.example.tpi_prog4_g10.repository.UsuarioRepository;
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

    
    @Transactional
    public Puja registrarPuja(Long subastaId, Long usuarioId, BigDecimal montoOfrecido) {
        
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RuntimeException("Error: Subasta no encontrada."));
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado."));

        
        if (subasta.getEstado() != EstadoSubasta.ACTIVA) {
            throw new RuntimeException("Error: Solo se puede pujar en subastas ACTIVAS.");
        }

        
        if (Instant.now().isAfter(subasta.getFechaCierre())) {
            throw new RuntimeException("Error: La subasta ya ha cerrado.");
        }

        
        if (subasta.getProducto().getVendedor().getId().equals(usuario.getId())) {
            throw new RuntimeException("Error: El vendedor no puede pujar en su propia subasta.");
        }

        
        BigDecimal montoMinimoRequerido;
        
        
        if (subasta.getMontoActual() == null) {
            montoMinimoRequerido = subasta.getPrecioBase();
        } else {
            
            montoMinimoRequerido = subasta.getMontoActual().add(new BigDecimal("1.00")); 
        }

        if (montoOfrecido.compareTo(montoMinimoRequerido) < 0) {
            throw new RuntimeException("Error: El monto ofrecido debe ser mayor o igual a " + montoMinimoRequerido);
        }

        
        Puja nuevaPuja = Puja.builder()
                .subasta(subasta)
                .usuario(usuario)
                .monto(montoOfrecido)
                .fechaHora(Instant.now())
                .build();
        
        pujaRepository.save(nuevaPuja);

        
        subasta.setMontoActual(montoOfrecido);
        subasta.setGanador(usuario);
        subastaRepository.save(subasta);

        return nuevaPuja;
    }
    
    
    public List<Puja> obtenerPujasPorUsuario(Long usuarioId) {
        
        return null; 
    }
}