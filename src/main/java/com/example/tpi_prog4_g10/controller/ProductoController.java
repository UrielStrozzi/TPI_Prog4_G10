package com.example.tpi_prog4_g10.controller;

import com.example.tpi_prog4_g10.dto.request.ProductoRequest;
import com.example.tpi_prog4_g10.dto.request.response.ProductoResponse;
import com.example.tpi_prog4_g10.model.Categoria;
import com.example.tpi_prog4_g10.model.Producto;
import com.example.tpi_prog4_g10.repository.CategoriaRepository;
import com.example.tpi_prog4_g10.repository.ProductoRepository;
import com.example.tpi_prog4_g10.service.ProductoService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired 
    private ProductoRepository productoRepository;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SELLER', 'ROLE_SELLER')")
    public ResponseEntity<ProductoResponse> crear(
            @Valid @RequestBody ProductoRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(productoService.crear(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SELLER', 'ADMIN', 'ROLE_SELLER', 'ROLE_ADMIN')")
    public ResponseEntity<?> editar(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody ProductoRequest request) {

        Producto producto = productoService.obtenerPorId(id);

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));

        if (!esAdmin && !producto.getVendedor().getUsername().equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tenés permiso para editar este producto.");
        }

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada."));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setCondicion(request.getCondicion());
        producto.setCategoria(categoria);

        return ResponseEntity.ok(productoService.guardarProducto(producto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SELLER', 'ADMIN', 'ROLE_SELLER', 'ROLE_ADMIN')")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            Authentication authentication) {

        Producto producto = productoService.obtenerPorId(id);

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN")); 

        if (!esAdmin && !producto.getVendedor().getUsername().equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tenés permiso para eliminar este producto.");
        }

        productoService.eliminar(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping
    public List<ProductoResponse> listarTodos() {
        return productoRepository.findAll().stream()
            // ← CORREGIDO ACÁ: Solo enviamos al frontend los productos que NO fueron borrados
            .filter(producto -> producto.getDeletedAt() == null) 
            .map(producto -> productoService.toResponse(producto))
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }
}