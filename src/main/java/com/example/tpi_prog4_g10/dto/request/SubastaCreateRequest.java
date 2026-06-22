package com.example.tpi_prog4_g10.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class SubastaCreateRequest {
    private Long productoId;
    private BigDecimal precioBase;
    private BigDecimal incrementoMinimo;
    private Instant fechaInicio;
    private Instant fechaFin;
}