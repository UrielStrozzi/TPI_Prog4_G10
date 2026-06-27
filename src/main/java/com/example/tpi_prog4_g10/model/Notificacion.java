package com.example.tpi_prog4_g10.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import com.example.tpi_prog4_g10.enums.EstadoNotificacion;
import com.example.tpi_prog4_g10.enums.TipoNotificacion;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "notificaciones", indexes = {
        @Index(name = "idx_notif_usuario_estado", columnList = "usuario_id, estado"),
        @Index(name = "idx_notif_usuario_fecha", columnList = "usuario_id, created_at"),
        @Index(name = "idx_notif_subasta_id", columnList = "subasta_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    // ── PK ────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relaciones ────────────────────────────────────────────

    // Destinatario (obligatorio)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Subasta de contexto (puede ser null — ej: notificaciones de sistema)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subasta_id", nullable = true)
    private Subasta subasta;

    // ── Tipo de evento que disparó la notificación ────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoNotificacion tipo;

    // ── Contenido ─────────────────────────────────────────────
    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String cuerpo;

    // ── Estado de lectura ─────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoNotificacion estado = EstadoNotificacion.PENDIENTE;

    @Column(name = "leida_at")
    private Instant leidaAt; // se llena al pasar a LEIDA

    // ── Auditoría ─────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ── Métodos de dominio ────────────────────────────────────

    /**
     * Marca la notificación como leída si todavía no lo estaba.
     * Registra el timestamp en leidaAt.
     */
    public void marcarComoLeida() {
        if (this.estado == EstadoNotificacion.PENDIENTE) {
            this.estado = EstadoNotificacion.LEIDA;
            this.leidaAt = Instant.now();
        }
    }

    public void archivar() {
        if (this.estado != EstadoNotificacion.ARCHIVADA) {
            this.estado = EstadoNotificacion.ARCHIVADA;
        }
    }

    /** Conveniencia para el service — ¿el usuario ya la vio? */
    public boolean fueLeida() {
        return this.estado != EstadoNotificacion.PENDIENTE;
    }
}