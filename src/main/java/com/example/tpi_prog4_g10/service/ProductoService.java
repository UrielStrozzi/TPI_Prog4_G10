package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.model.Categoria;
import com.example.tpi_prog4_g10.model.Producto;
import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.repository.CategoriaRepository;
import com.example.tpi_prog4_g10.repository.ProductoRepository;
import com.example.tpi_prog4_g10.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.tpi_prog4_g10.dto.request.ProductoRequest;
import com.example.tpi_prog4_g10.dto.request.response.ProductoResponse;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

public ProductoResponse crear(ProductoRequest request, String loginVendedor) {
    Usuario vendedor = usuarioRepository.findByEmail(loginVendedor)
        .or(() -> usuarioRepository.findByUsername(loginVendedor))
        .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

    Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
        .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

    Producto producto = Producto.builder()
        .nombre(request.getNombre())
        .descripcion(request.getDescripcion())
        .condicion(request.getCondicion())
        .categoria(categoria)
        .vendedor(vendedor)
        .build();

    return toResponse(productoRepository.save(producto));
}

    public ProductoResponse toResponse(Producto p) {
    return ProductoResponse.builder()
        .id(p.getId())
        .nombre(p.getNombre())
        .descripcion(p.getDescripcion())
        .condicion(p.getCondicion())
        .categoriaNombre(p.getCategoria().getNombre())
        .vendedorNombre(p.getVendedor().getNombre())
        .vendedorEmail(p.getVendedor().getEmail())
        .createdAt(p.getCreatedAt())
        .build();
}
    
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Producto no encontrado. ID: " + id));
    }

    
    public void eliminar(Long id) {
        Producto producto = obtenerPorId(id);
        producto.eliminar(); // setea deletedAt = Instant.now()
        productoRepository.save(producto);
    }
}