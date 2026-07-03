package com.example.tpi_prog4_g10.dto.request.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String nombre;
    private List<String> roles;
}