package com.example.tpi_prog4_g10.dto.request;

import com.example.tpi_prog4_g10.enums.CondicionProducto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoRequest {
    private String titulo;
    private String descripcion;
    private CondicionProducto condicion;  // NUEVO | USADO | REACONDICIONADO
    private Long categoriaId;
}