package com.example.tpi_prog4_g10.config;

import io.jsonwebtoken.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    
    private final SecretKey jwtSecret = Jwts.SIG.HS512.key().build();
    
    private final int jwtExpirationMs = 86400000; 

    
    public String generarJwtToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();

        
        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(jwtSecret)
                .compact();
    }

    
    public String obtenerNombreUsuarioDesdeJwt(String token) {
        
        return Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload() 
                .getSubject();
    }

    
    public boolean validarJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}