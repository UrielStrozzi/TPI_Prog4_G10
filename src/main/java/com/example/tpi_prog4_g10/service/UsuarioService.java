package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.model.Rol;
import com.example.tpi_prog4_g10.enums.NombreRol;
import com.example.tpi_prog4_g10.repository.UsuarioRepository;
import com.example.tpi_prog4_g10.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario registrarUsuario(Usuario usuario) {
        
        
        if (usuarioRepository.existsByNombre(usuario.getNombre())) {
            throw new RuntimeException("Error: El nombre de usuario ya está en uso.");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Error: El email ya está en uso.");
        }

    
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        
        Rol rolUser = rolRepository.findByNombre(NombreRol.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado en la base de datos."));
        
        usuario.getRoles().add(rolUser);

        
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorNombre(String nombre) {
        return usuarioRepository.findByNombre(nombre);
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }
}