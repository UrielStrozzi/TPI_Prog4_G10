package com.example.tpi_prog4_g10.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios", indexes = {
        @Index(name = "idx_usuarios_email", columnList = "email"),
        @Index(name = "idx_usuarios_username", columnList = "username"),
        @Index(name = "idx_usuarios_bloqueado", columnList = "bloqueado"),
        @Index(name = "idx_usuarios_deleted_at", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido;

    @Column(length = 30)
    private String telefono;

    @Column(nullable = false)
    @Builder.Default
    private boolean bloqueado = false;

    @Column(name = "motivo_bloqueo", length = 500)
    private String motivoBloqueo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bloqueado_por")
    private Usuario bloqueadoPor;

    @Column(name = "bloqueado_at")
    private Instant bloqueadoAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();

    // ── Métodos de dominio ────────────────────────────────────
    public boolean estaEliminado() {
        return this.deletedAt != null;
    }

    public void bloquear(String motivo, Usuario admin) {
        this.bloqueado = true;
        this.motivoBloqueo = motivo;
        this.bloqueadoPor = admin;
        this.bloqueadoAt = Instant.now();
    }

    public void desbloquear() {
        this.bloqueado = false;
        this.motivoBloqueo = null;
        this.bloqueadoPor = null;
        this.bloqueadoAt = null;
    }
}