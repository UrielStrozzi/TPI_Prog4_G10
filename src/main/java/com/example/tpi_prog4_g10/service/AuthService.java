package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.config.JwtUtils;
import com.example.tpi_prog4_g10.dto.request.LoginRequest;
import com.example.tpi_prog4_g10.dto.request.RegisterRequest;
import com.example.tpi_prog4_g10.dto.request.response.AuthResponse;
import com.example.tpi_prog4_g10.enums.NombreRol;
import com.example.tpi_prog4_g10.model.Rol;
import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.repository.RolRepository;
import com.example.tpi_prog4_g10.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils; 

    public AuthResponse registrar(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Rol rolUser = rolRepository.findByNombre(NombreRol.USER)
            .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));

        Usuario usuario = Usuario.builder()
            .nombre(request.getNombre())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .roles(Set.of(rolUser))
            .build();

        usuarioRepository.save(usuario);

        
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getNombre(), request.getPassword())
        );
        String token = jwtUtils.generarJwtToken(auth);

        return new AuthResponse(token, "Bearer", usuario.getEmail(),
            List.of(rolUser.getNombre().name()));
    }

    public AuthResponse login(LoginRequest request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getLogin(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(auth);

        String token = jwtUtils.generarJwtToken(auth);

        User userDetails = (User) auth.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new AuthResponse(
                token,
                "Bearer",
                userDetails.getUsername(),
                roles);
    }
}