package com.example.tpi_prog4_g10.dto.request.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String username;
    private String nombre;
    private String email;
    private boolean bloqueado;
    private Set<String> roles; 
}