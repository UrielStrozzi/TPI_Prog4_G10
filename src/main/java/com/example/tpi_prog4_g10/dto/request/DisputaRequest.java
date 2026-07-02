package com.example.tpi_prog4_g10.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisputaRequest {
    private Long subastaId;
    private String motivo;
    private String descripcion;
}
