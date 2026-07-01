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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> crear(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProductoRequest request) {

        Usuario vendedor = usuarioRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado."));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada."));

        Producto producto = Producto.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .condicion(request.getCondicion())
                .categoria(categoria)
                .vendedor(vendedor)
                .build();

        Producto nuevoProducto = productoService.guardarProducto(producto);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> editar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProductoRequest request) {

        Producto producto = productoService.obtenerPorId(id);

        // Solo el vendedor dueño puede editar
        if (!producto.getVendedor().getUsername().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tenés permiso para editar este producto.");
        }

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada."));

        producto.setTitulo(request.getTitulo());
        producto.setDescripcion(request.getDescripcion());
        producto.setCondicion(request.getCondicion());
        producto.setCategoria(categoria);

        return ResponseEntity.ok(productoService.guardarProducto(producto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Producto producto = productoService.obtenerPorId(id);

        boolean esAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // SELLER solo puede eliminar sus propios productos
        if (!esAdmin && !producto.getVendedor().getUsername().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tenés permiso para eliminar este producto.");
        }

        productoService.eliminar(id);
        return ResponseEntity.noContent().build(); // 204
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