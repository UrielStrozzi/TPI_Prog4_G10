package com.example.tpi_prog4_g10.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

import com.example.tpi_prog4_g10.enums.EstadoSubasta;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "historial_estados_subasta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subasta_id", nullable = false)
    @JsonIgnore // ← corta recursión subasta → historial → subasta
    private Subasta subasta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private EstadoSubasta estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoSubasta estadoNuevo;

    @Column(nullable = false)
    private Instant fecha;

    @Column(columnDefinition = "TEXT") // ← nullable, sin nullable = false
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore // ← corta recursión usuario → historial → usuario
    private Usuario usuarioResponsable;

    // Campo extra para el frontend — username sin exponer el objeto Usuario
    // completo
    @Transient // no se persiste en BD
    public String getUsuarioUsername() {
        return usuarioResponsable != null ? usuarioResponsable.getUsername() : null;
    }
}