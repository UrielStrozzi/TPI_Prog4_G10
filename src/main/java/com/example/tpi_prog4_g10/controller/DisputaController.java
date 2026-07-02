package com.example.tpi_prog4_g10.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.tpi_prog4_g10.service.DisputaService;
import com.example.tpi_prog4_g10.dto.request.*;

import com.example.tpi_prog4_g10.model.Disputa;

@RestController
@RequestMapping("/api/disputas")
public class DisputaController {

    @Autowired
    private DisputaService disputaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'USER')")
    public ResponseEntity<?> abrirDisputa(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DisputaRequest request) {

        Disputa disputa = disputaService.abrirDisputa(
                request.getSubastaId(),
                userDetails.getUsername(),
                request.getMotivo(),
                request.getDescripcion());

        return new ResponseEntity<>(disputa, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'USER', 'ADMIN')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(disputaService.obtenerPorId(id));
    }

    @PutMapping("/{id}/resolver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resolver(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ResolverDisputaRequest request) {

        Disputa disputa = disputaService.resolverDisputa(
                id,
                userDetails.getUsername(),
                request.getResolucion(),
                request.getEstadoFinalSubasta());

        return ResponseEntity.ok(disputa);
    }
}
