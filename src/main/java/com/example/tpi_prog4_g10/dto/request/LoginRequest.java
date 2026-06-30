package com.example.tpi_prog4_g10.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String login; // email o username
    private String password;
}