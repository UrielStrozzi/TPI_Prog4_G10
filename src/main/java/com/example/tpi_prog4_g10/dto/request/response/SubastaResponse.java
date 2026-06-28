package com.example.tpi_prog4_g10.dto.request.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
public class SubastaResponse {
    private Long id;
    private String productoTitulo;
    private BigDecimal precioBase;
    private BigDecimal montoActual;
    private BigDecimal incrementoMinimo;
    private Instant fechaInicio;
    private Instant fechaFin;
    private String estado;
    private String nombreGanadorParcial; 
}