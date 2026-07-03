package com.example.tpi_prog4_g10.dto.request.response;

import com.example.tpi_prog4_g10.enums.CondicionProducto;
import com.example.tpi_prog4_g10.enums.EstadoProducto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductoResponse {
    private Long id;
    private String nombre;
    private CondicionProducto condicion;
    private String descripcion;
    private List<String> imagenes;
    private EstadoProducto estado;
    private String categoriaNombre;
    private String vendedorNombre;
    private String vendedorEmail;
    private LocalDateTime fechaCreacion;
    private Instant createdAt;
}