package com.example.tpi_prog4_g10.controller;

import com.example.tpi_prog4_g10.dto.request.PujaRequest;
import com.example.tpi_prog4_g10.dto.request.response.MensajeResponse;
import com.example.tpi_prog4_g10.service.PujaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subastas")
public class PujaController {

    @Autowired
    private PujaService pujaService;

    
    @PostMapping("/{subastaId}/pujas")
    public ResponseEntity<MensajeResponse> ofertar(
            @PathVariable Long subastaId,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestBody PujaRequest request) {

        pujaService.registrarPuja(subastaId, usuarioId, request.getMonto());
        
        return new ResponseEntity<>(new MensajeResponse("¡Puja registrada con éxito! Sos el máximo postor."), HttpStatus.CREATED);
    }
}