package com.example.tpi_prog4_g10.controller;

import com.example.tpi_prog4_g10.dto.request.SubastaCreateRequest;
import com.example.tpi_prog4_g10.dto.request.response.SubastaResponse;
import com.example.tpi_prog4_g10.enums.EstadoSubasta;
import com.example.tpi_prog4_g10.model.Producto;
import com.example.tpi_prog4_g10.model.Subasta;
import com.example.tpi_prog4_g10.repository.PujaRepository;
import com.example.tpi_prog4_g10.service.ProductoService;
import com.example.tpi_prog4_g10.service.SubastaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subastas")
public class SubastaController {

    @Autowired
    private SubastaService subastaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private PujaRepository pujaRepository;

    @PostMapping
    public ResponseEntity<SubastaResponse> crear(@RequestBody SubastaCreateRequest request) {
        Producto producto = productoService.obtenerPorId(request.getProductoId());

        Subasta subasta = Subasta.builder()
                .producto(producto)
                .vendedor(producto.getVendedor())
                .precioBase(request.getPrecioBase())
                .incrementoMinimo(request.getIncrementoMinimo())
                .fechaInicio(request.getFechaInicio())
                .fechaCierre(request.getFechaCierre())
                .build();

        Subasta nuevaSubasta = subastaService.crearSubasta(subasta);
        return new ResponseEntity<>(convertirADto(nuevaSubasta), HttpStatus.CREATED);
    }

    
    @PostMapping("/{id}/publicar")
    public ResponseEntity<SubastaResponse> publicar(@PathVariable Long id) {
        Subasta subastaPublicada = subastaService.publicarSubasta(id);
        return ResponseEntity.ok(convertirADto(subastaPublicada));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<SubastaResponse> cancelar(
        
            @PathVariable Long id,
            @RequestParam(required = false) String motivo,
            Authentication authentication) {

        boolean esAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        System.out.println("Roles del usuario: " + authentication.getAuthorities());
        System.out.println("Es admin: " + esAdmin);
        Subasta subastaCancelada = subastaService.cancelarSubasta(id, motivo, authentication.getName(), esAdmin);
        return ResponseEntity.ok(convertirADto(subastaCancelada));
    }

    @GetMapping
    public ResponseEntity<List<SubastaResponse>> obtenerTodas() {
        List<SubastaResponse> respuestas = subastaService.obtenerTodas().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuestas);
    }

    
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SubastaResponse>> obtenerPorEstado(@PathVariable EstadoSubasta estado) {
        List<SubastaResponse> respuestas = subastaService.obtenerPorEstado(estado).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuestas);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<SubastaResponse> obtenerPorId(@PathVariable Long id) {
        Subasta subasta = subastaService.obtenerPorId(id);
        return ResponseEntity.ok(convertirADto(subasta));
    }

    
private SubastaResponse convertirADto(Subasta subasta) {
    return SubastaResponse.builder()
            .id(subasta.getId())
            .productoNombre(subasta.getProducto().getNombre())
            .precioBase(subasta.getPrecioBase())
            .montoActual(subasta.getMontoActual())
            .incrementoMinimo(subasta.getIncrementoMinimo())
            .fechaInicio(subasta.getFechaInicio())
            .fechaFin(subasta.getFechaCierre())
            .estado(subasta.getEstado().name())
            .nombreGanadorParcial(subasta.getGanador() != null ? subasta.getGanador().getNombre() : null)
            .totalPujas(pujaRepository.countBySubastaId(subasta.getId()))                       // ← nuevo
            .vendedorUsername(subasta.getVendedor().getUsername())                 // ← nuevo
            .build();
}
}