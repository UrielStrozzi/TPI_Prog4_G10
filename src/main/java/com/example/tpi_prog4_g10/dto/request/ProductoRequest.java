package com.example.tpi_prog4_g10.dto.request;

import java.util.ArrayList;
import java.util.List;

import com.example.tpi_prog4_g10.enums.CondicionProducto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductoRequest {
    @NotBlank
    @Size(max = 100)
    private String nombre;        // era titulo

    private String descripcion;
    private List<String> imagenes = new ArrayList<>();

    @NotNull
    private Long categoriaId;

    @NotNull
    private CondicionProducto condicion;
}