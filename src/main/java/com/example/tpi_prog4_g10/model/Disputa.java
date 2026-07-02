package com.example.tpi_prog4_g10.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import com.example.tpi_prog4_g10.enums.EstadoDisputa;

@Entity
@Table(name = "disputas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disputa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subasta_id", nullable = false, unique = true)
    private Subasta subasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iniciador_id", nullable = false)
    private Usuario reclamante;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "fecha_apertura", nullable = false)
    private Instant fechaApertura;

    @Column(columnDefinition = "TEXT")
    private String resolucion;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoDisputa estado = EstadoDisputa.ABIERTA;

    @Column(name = "fecha_resolucion")
    private Instant fechaResolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resuelto_por_id")
    private Usuario resueltoPor;
}