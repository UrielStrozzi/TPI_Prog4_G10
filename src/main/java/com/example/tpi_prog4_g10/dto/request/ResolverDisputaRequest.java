package com.example.tpi_prog4_g10.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResolverDisputaRequest {
    private String resolucion;
    private String estadoFinalSubasta; // "ADJUDICADA" | "FINALIZADA" | "CANCELADA"
}
