package com.example.tpi_prog4_g10.dto.request;

import lombok.Data;

@Data
public class ResolverDisputaRequest {
    private String resolucion;
    private String estadoFinalSubasta; // "ADJUDICADA" | "FINALIZADA" | "CANCELADA"
}
