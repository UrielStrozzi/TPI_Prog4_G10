package com.example.tpi_prog4_g10.model;

import com.example.tpi_prog4_g10.enums.EstadoSubasta;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "subastas", indexes = {
        @Index(name = "idx_subastas_estado", columnList = "estado"),
        @Index(name = "idx_subastas_estado_inicio", columnList = "estado, fecha_inicio"),
        @Index(name = "idx_subastas_estado_cierre", columnList = "estado, fecha_cierre"),
        @Index(name = "idx_subastas_vendedor_id", columnList = "vendedor_id"),
        @Index(name = "idx_subastas_ganador_id", columnList = "ganador_id"),
        @Index(name = "idx_subastas_producto_id", columnList = "producto_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subasta {

    // ── PK ────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relaciones ────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ganador_id")
    private Usuario ganador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelado_por_id")
    private Usuario canceladoPor;

    // ── Configuración ─────────────────────────────────────────
    @Column(name = "precio_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "incremento_minimo", nullable = false, precision = 15, scale = 2)
    private BigDecimal incrementoMinimo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio;

    @Column(name = "fecha_cierre", nullable = false)
    private Instant fechaCierre;

    // ── Estado ────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoSubasta estado = EstadoSubasta.BORRADOR;

    // ── Resultado ─────────────────────────────────────────────
    @Column(name = "monto_actual", precision = 15, scale = 2)
    private BigDecimal montoActual;

    @Column(name = "fecha_adjudicacion")
    private Instant fechaAdjudicacion;

    @Column(name = "precio_final", precision = 15, scale = 2)
    private BigDecimal precioFinal;

    // ── Cancelación ───────────────────────────────────────────
    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Column(name = "fecha_cancelacion")
    private Instant fechaCancelacion;

    // ── Auditoría ─────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ── Métodos de dominio ────────────────────────────────────
    public boolean estaActiva() {
        return this.estado == EstadoSubasta.ACTIVA
                && Instant.now().isBefore(this.fechaCierre);
    }

    public boolean tienePujas() {
        return this.montoActual != null;
    }

    public void cancelar(String motivo, Usuario responsable) {
        this.estado = EstadoSubasta.CANCELADA;
        this.motivoCancelacion = motivo;
        this.canceladoPor = responsable;
        this.fechaCancelacion = Instant.now();
    }

    public void adjudicar(Usuario ganador) {
        this.estado = EstadoSubasta.ADJUDICADA;
        this.ganador = ganador;
        this.precioFinal = this.montoActual;
        this.fechaAdjudicacion = Instant.now();
    }
}