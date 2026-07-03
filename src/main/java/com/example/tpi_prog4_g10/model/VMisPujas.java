package com.example.tpi_prog4_g10.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Immutable
@Table(name = "v_mis_pujas")
@Getter
@NoArgsConstructor
public class VMisPujas {

    @Id
    @Column(name = "puja_id")
    private Long pujaId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "subasta_id")
    private Long subastaId;

    @Column(name = "monto")
    private BigDecimal monto;

    @Column(name = "fecha_hora")
    private Instant fechaHora;

    @Column(name = "estado_puja")
    private String estadoPuja;

    @Column(name = "estado_subasta")
    private String estadoSubasta;

    @Column(name = "monto_actual")
    private BigDecimal montoActual;

    @Column(name = "es_oferta_lider", columnDefinition = "INT")
    private Integer esOfertaLider;

    public boolean esLider() {
    return Integer.valueOf(1).equals(this.esOfertaLider);
    }
}