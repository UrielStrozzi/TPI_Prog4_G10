package com.example.tpi_prog4_g10.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.tpi_prog4_g10.enums.CondicionProducto;

import java.time.Instant;

@Entity
@Table(name = "productos", indexes = {
        @Index(name = "idx_productos_vendedor_id", columnList = "vendedor_id"),
        @Index(name = "idx_productos_categoria_id", columnList = "categoria_id"),
        @Index(name = "idx_productos_deleted_at", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    // ── PK ────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relaciones ────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // ── Datos del producto ────────────────────────────────────
    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CondicionProducto condicion = CondicionProducto.USADO;

    // ── Soft delete ───────────────────────────────────────────
    @Column(name = "deleted_at")
    private Instant deletedAt; // NULL = activo, non-NULL = eliminado

    // ── Auditoría ─────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ── Métodos de dominio ────────────────────────────────────
    public boolean estaEliminado() {
        return this.deletedAt != null;
    }

    public void eliminar() {
        this.deletedAt = Instant.now();
    }
}