package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.model.Producto;
import com.example.tpi_prog4_g10.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    
    public List<Producto> obtenerTodos() {
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