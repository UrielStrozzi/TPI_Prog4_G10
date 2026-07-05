package com.example.tpi_prog4_g10.controller;

import com.example.tpi_prog4_g10.dto.request.PujaRequest;
import com.example.tpi_prog4_g10.dto.request.response.MensajeResponse;
import com.example.tpi_prog4_g10.service.PujaService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.tpi_prog4_g10.repository.UsuarioRepository;
import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.model.VMisPujas;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/pujas")
@RequiredArgsConstructor
public class PujaController {

        private final PujaService pujaService;
        private final UsuarioRepository usuarioRepository;

        @PostMapping("/{subastaId}/pujas")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<MensajeResponse> ofertar(
                        @PathVariable Long subastaId,
                        @AuthenticationPrincipal UserDetails userDetails,
                        @RequestBody PujaRequest request) {

                Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

                pujaService.registrarPuja(subastaId, usuario.getId(), request.getMonto());

                return new ResponseEntity<>(
                                new MensajeResponse("¡Puja registrada con éxito! Sos el máximo postor."),
                                HttpStatus.CREATED);
        }

        @GetMapping("/mias")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<List<VMisPujas>> misPujas(Authentication authentication) {

                Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                                .or(() -> usuarioRepository.findByUsername(authentication.getName()))
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                System.out.println("Usuario id: " + usuario.getId());
                System.out.println("Username: " + authentication.getName());
                return ResponseEntity.ok(pujaService.obtenerMisPujas(usuario.getId()));
        }
}