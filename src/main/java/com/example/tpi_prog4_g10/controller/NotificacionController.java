package com.example.tpi_prog4_g10.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.example.tpi_prog4_g10.service.*;
import com.example.tpi_prog4_g10.repository.*;
import com.example.tpi_prog4_g10.model.*;
import com.example.tpi_prog4_g10.dto.request.response.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<List<Notificacion>> obtenerMisNotificaciones(
            @AuthenticationPrincipal UserDetails userDetails) {

    Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
        .or(() -> usuarioRepository.findByUsername(userDetails.getUsername()))
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        return ResponseEntity.ok(notificacionService.obtenerPorUsuario(usuario.getId()));
    }

    @PutMapping("/{id}/leer")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok(new MensajeResponse("Notificación marcada como leída."));
    }
}