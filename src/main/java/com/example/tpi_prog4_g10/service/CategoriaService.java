package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.dto.request.CategoriaRequest;
import com.example.tpi_prog4_g10.dto.request.response.CategoriaResponse;
import com.example.tpi_prog4_g10.model.Categoria;
import com.example.tpi_prog4_g10.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public CategoriaResponse crear(CategoriaRequest request) {
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }
        Categoria categoria = Categoria.builder()
            .nombre(request.getNombre())
            .descripcion(request.getDescripcion())
            .build();
        return toResponse(categoriaRepository.save(categoria));
    }

    public Categoria obtenerEntidad(Long id) {
        return categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + id));
    }

    private CategoriaResponse toResponse(Categoria c) {
        return CategoriaResponse.builder()
            .id(c.getId())
            .nombre(c.getNombre())
            .descripcion(c.getDescripcion())
            .totalProductos(c.getProductos() != null ? c.getProductos().size() : 0) 
            .build();
    }
}