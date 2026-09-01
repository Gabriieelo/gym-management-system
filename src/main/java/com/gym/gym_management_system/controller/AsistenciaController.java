package com.gym.gym_management_system.controller;

import com.gym.gym_management_system.dto.AsistenciaRequest;
import com.gym.gym_management_system.dto.AsistenciaResponse;
import com.gym.gym_management_system.service.AsistenciaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @PostMapping
    public ResponseEntity<AsistenciaResponse> registrar(
            @Valid @RequestBody AsistenciaRequest request) {
        AsistenciaResponse asistencia = asistenciaService.registrar(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(asistencia.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(asistencia);
    }

    @GetMapping
    public ResponseEntity<List<AsistenciaResponse>> listarPorFecha(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(asistenciaService.listarPorFecha(fecha));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<AsistenciaResponse>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(asistenciaService.listarPorCliente(clienteId));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<AsistenciaResponse> anular(@PathVariable Long id) {
        return ResponseEntity.ok(asistenciaService.anular(id));
    }
}
