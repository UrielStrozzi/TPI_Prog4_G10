package com.example.tpi_prog4_g10.controller;

import com.example.tpi_prog4_g10.dto.request.ProductoRequest;
import com.example.tpi_prog4_g10.model.Categoria;
import com.example.tpi_prog4_g10.model.Producto;
import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.repository.CategoriaRepository;
import com.example.tpi_prog4_g10.repository.UsuarioRepository;
import com.example.tpi_prog4_g10.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    
    @PostMapping
    public ResponseEntity<?> crear(
            @RequestHeader("X-Usuario-Id") Long vendedorId,
            @RequestBody ProductoRequest request) {

        Usuario vendedor = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new RuntimeException("Error: Vendedor no encontrado."));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Error: Categoría no encontrada."));

        Producto producto = Producto.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .categoria(categoria)
                .vendedor(vendedor)
                .build();

        Producto nuevoProducto = productoService.guardarProducto(producto);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }
}