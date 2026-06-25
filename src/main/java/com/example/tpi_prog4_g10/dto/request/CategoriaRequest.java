package com.example.tpi_prog4_g10.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class CategoriaRequest {

    @NotBlank
    @Size(max = 80)
    private String nombre;

    private String descripcion;
}