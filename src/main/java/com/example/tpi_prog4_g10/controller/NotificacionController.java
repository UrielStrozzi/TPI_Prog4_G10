package com.example.tpi_prog4_g10.controller;

import com.example.tpi_prog4_g10.model.Notificacion;
import com.example.tpi_prog4_g10.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    @Autowired
    private NotificacionRepository notificacionRepository; 

    
    @GetMapping("/mis-notificaciones")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Notificacion>> obtenerMisNotificaciones(@AuthenticationPrincipal UserDetails userDetails) {
        
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioUsername(userDetails.getUsername());
        return ResponseEntity.ok(notificaciones);
    }

    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notificacion> crearNotificacion(@RequestBody Notificacion notificacion) {
        return ResponseEntity.ok(notificacionRepository.save(notificacion));
    }

    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        notificacionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}