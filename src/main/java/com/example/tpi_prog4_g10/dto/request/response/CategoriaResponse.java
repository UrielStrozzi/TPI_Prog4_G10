package com.example.tpi_prog4_g10.dto.request.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoriaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private int totalProductos;
}