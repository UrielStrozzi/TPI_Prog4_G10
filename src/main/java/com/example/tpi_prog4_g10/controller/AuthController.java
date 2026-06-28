package com.example.tpi_prog4_g10.controller;

import com.example.tpi_prog4_g10.config.JwtUtils;
import com.example.tpi_prog4_g10.dto.request.LoginRequest;
import com.example.tpi_prog4_g10.dto.request.RegisterRequest;
import com.example.tpi_prog4_g10.dto.request.response.MensajeResponse;
import com.example.tpi_prog4_g10.dto.request.response.JwtResponse; 
import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    
    @PostMapping("/registro")
    public ResponseEntity<MensajeResponse> registrarUsuario(@RequestBody RegisterRequest request) {
        
        Usuario nuevoUsuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .passwordHash(request.getPassword()) 
                .build();

        usuarioService.registrarUsuario(nuevoUsuario);

        return new ResponseEntity<>(new MensajeResponse("Usuario registrado con éxito."), HttpStatus.CREATED);
    }

    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getNombre(), request.getPassword())
        );

        
        SecurityContextHolder.getContext().setAuthentication(authentication);

        
        String jwt = jwtUtils.generarJwtToken(authentication);
        
        
        User userDetails = (User) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

    
        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), roles));
    }
}