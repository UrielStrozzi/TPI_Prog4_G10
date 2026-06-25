package com.example.tpi_prog4_g10.dto.request.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tipo; 
    private String email;
    private List<String> roles;
}