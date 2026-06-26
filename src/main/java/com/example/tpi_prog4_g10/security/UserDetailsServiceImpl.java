package com.example.tpi_prog4_g10.security;

import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String loginInput) throws UsernameNotFoundException {
        
        Usuario usuario = usuarioRepository.findByNombre(loginInput)
            .orElseGet(() -> usuarioRepository.findByEmail(loginInput)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con nombre o email: " + loginInput)));

        List<GrantedAuthority> authorities = usuario.getRoles().stream()
            .map(rol -> new SimpleGrantedAuthority(rol.getNombre().name()))
            .collect(Collectors.toList());

        
        return new org.springframework.security.core.userdetails.User(
            usuario.getEmail(),
            usuario.getPassword(),
            authorities
        );
    }
}