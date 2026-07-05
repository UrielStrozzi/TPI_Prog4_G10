package com.example.tpi_prog4_g10.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(unique = true, nullable = false, length = 50)
    private String nombre;

    private String descripcion;
    
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    @JsonIgnore  // ← corta la recursión
    @Builder.Default
    private List<Producto> productos = new ArrayList<>();
}