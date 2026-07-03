package com.example.tpi_prog4_g10.dto.request.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class SubastaResponse {
    private Long id;
    private String productoNombre;
    private BigDecimal precioBase;
    private BigDecimal montoActual;
    private BigDecimal incrementoMinimo;
    private Instant fechaInicio;
    private Instant fechaFin;
    private String estado;
    private String nombreGanadorParcial; 
    private Long totalPujas;          // ← nuevo
    private String vendedorUsername;  // ← nuevo
}