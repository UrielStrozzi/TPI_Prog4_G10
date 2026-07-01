package com.example.tpi_prog4_g10.model;

import com.example.tpi_prog4_g10.enums.EstadoPuja;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pujas", indexes = {
        @Index(name = "idx_pujas_subasta_estado_monto", columnList = "subasta_id, estado, monto"),
        @Index(name = "idx_pujas_subasta_fecha", columnList = "subasta_id, fecha_hora"),
        @Index(name = "idx_pujas_usuario_id", columnList = "usuario_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Puja {

    // ── PK ────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relaciones ────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subasta_id", nullable = false)
    private Subasta subasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // ── Datos de la puja ──────────────────────────────────────
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @CreationTimestamp
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private Instant fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPuja estado = EstadoPuja.CONFIRMADA;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    // ── Métodos de dominio ────────────────────────────────────
    public void anular() {
        this.estado = EstadoPuja.ANULADA;
    }

    public boolean estaConfirmada() {
        return this.estado == EstadoPuja.CONFIRMADA;
    }

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) {
            fechaHora = Instant.now();
        }
    }
}