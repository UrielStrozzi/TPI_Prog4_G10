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
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

@Override
public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
    log.info("Buscando usuario con login: {}", login);

    Usuario usuario = usuarioRepository.findByEmail(login)
            .or(() -> usuarioRepository.findByUsername(login))
            .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuario no encontrado: " + login));

    List<GrantedAuthority> authorities = usuario.getRoles().stream()
            .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre().name()))
            .collect(Collectors.toList());

    return new org.springframework.security.core.userdetails.User(
            usuario.getUsername(),
            usuario.getPasswordHash(),
            !usuario.estaEliminado(),
            true,
            true,
            !usuario.isBloqueado(),
            authorities
    );
}
}