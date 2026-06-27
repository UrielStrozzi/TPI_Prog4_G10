package com.example.tpi_prog4_g10.controller;

import com.example.tpi_prog4_g10.dto.request.response.UsuarioResponse;
import com.example.tpi_prog4_g10.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos()); 
    }

    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    
    @PutMapping("/{id}/bloquear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> bloquearUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.bloquearUsuario(id));
    }

    
    @PutMapping("/{id}/desbloquear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> desbloquearUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.desbloquearUsuario(id));
    }

    
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> asignarRoles(@PathVariable Long id, @RequestBody Set<String> nuevosRoles) {
        return ResponseEntity.ok(usuarioService.asignarRoles(id, nuevosRoles));
    }

    
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obtenerPerfilActual(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(usuarioService.buscarPorUsername(userDetails.getUsername()));
    }
}