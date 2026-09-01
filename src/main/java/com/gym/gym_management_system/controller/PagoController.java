package com.gym.gym_management_system.controller;

import com.gym.gym_management_system.dto.EstadoCuotaResponse;
import com.gym.gym_management_system.dto.PagoRequest;
import com.gym.gym_management_system.dto.PagoResponse;
import com.gym.gym_management_system.service.PagoService;
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
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    public ResponseEntity<PagoResponse> registrar(@Valid @RequestBody PagoRequest request) {
        PagoResponse pago = pagoService.registrar(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pago.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(pago);
    }

    @GetMapping
    public ResponseEntity<List<PagoResponse>> listarPorFecha(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(pagoService.listarPorFecha(fecha));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PagoResponse>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pagoService.listarPorCliente(clienteId));
    }

    @GetMapping("/cliente/{clienteId}/cuota-actual")
    public ResponseEntity<EstadoCuotaResponse> consultarCuotaActual(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pagoService.consultarCuotaActual(clienteId));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<PagoResponse> anular(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.anular(id));
    }
}
