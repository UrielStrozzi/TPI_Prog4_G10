package com.example.tpi_prog4_g10.enums;

public enum TipoNotificacion {
    SUBASTA_GANADA,        // al usuario ganador cuando se adjudica
    SUBASTA_ADJUDICADA,    // al vendedor cuando se adjudica
    PUJA_SUPERADA,         // al usuario cuya puja fue superada
    SUBASTA_CANCELADA,     // a todos los que pujaron
    DISPUTA_ABIERTA,       // al vendedor o ganador según quien la abrió
    DISPUTA_RESUELTA,      // a ambas partes al resolver
    SUBASTA_POR_CERRAR,    // recordatorio antes del cierre (opcional)
    SISTEMA                // mensajes genéricos del admin
}
