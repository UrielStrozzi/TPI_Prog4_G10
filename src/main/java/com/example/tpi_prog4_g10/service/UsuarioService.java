package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.model.Rol;
import com.example.tpi_prog4_g10.enums.NombreRol;
import com.example.tpi_prog4_g10.dto.request.response.UsuarioResponse;
import com.example.tpi_prog4_g10.repository.UsuarioRepository;
import com.example.tpi_prog4_g10.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    
    public Usuario registrarUsuario(Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new RuntimeException("Error: El nombre de usuario ya está en uso.");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Error: El email ya está en uso.");
        }

        usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));
        Rol rolUser = rolRepository.findByNombre(NombreRol.USER)
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado en la base de datos."));
        
        usuario.getRoles().add(rolUser);
        usuario.setBloqueado(false); 

        return usuarioRepository.save(usuario);
    }

    
    public UsuarioResponse buscarPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el nombre: " + username));
        return mapearADto(usuario);
    }

    
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return mapearADto(usuario);
    }

    
    public UsuarioResponse bloquearUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        usuario.setBloqueado(true);
        return mapearADto(usuarioRepository.save(usuario));
    }

    
    public UsuarioResponse desbloquearUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        usuario.setBloqueado(false);
        return mapearADto(usuarioRepository.save(usuario));
    }

    
    public UsuarioResponse asignarRoles(Long id, Set<String> nuevosRoles) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        Set<Rol> rolesEntidad = nuevosRoles.stream()
                .map(rolStr -> {
                    NombreRol nombreRolEnum = NombreRol.valueOf(rolStr); 
                    return rolRepository.findByNombre(nombreRolEnum)
                            .orElseThrow(() -> new RuntimeException("Rol no encontrado en BD: " + rolStr));
                })
                .collect(Collectors.toSet());
        
        usuario.setRoles(rolesEntidad);
        return mapearADto(usuarioRepository.save(usuario));
    }

    public Optional<Usuario> buscarPorNombre(String nombre) {
        return usuarioRepository.findByUsername(nombre);
    }

    
    private UsuarioResponse mapearADto(Usuario usuario) {
        UsuarioResponse dto = new UsuarioResponse();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername()); // ← faltaba esta línea
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setBloqueado(usuario.isBloqueado());

        if (usuario.getRoles() != null) {
            Set<String> rolesStr = usuario.getRoles().stream()
                    .map(rol -> rol.getNombre().name())
                    .collect(Collectors.toSet());
            dto.setRoles(rolesStr);
        }
        return dto;
    }

    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }
}