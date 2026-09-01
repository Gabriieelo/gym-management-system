package com.gym.gym_management_system.controller;

import com.gym.gym_management_system.dto.MovimientoCajaRequest;
import com.gym.gym_management_system.dto.MovimientoCajaResponse;
import com.gym.gym_management_system.dto.ResumenCajaResponse;
import com.gym.gym_management_system.dto.CierreCajaRequest;
import com.gym.gym_management_system.dto.CierreCajaResponse;
import com.gym.gym_management_system.service.CierreCajaService;
import com.gym.gym_management_system.service.MovimientoCajaService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/caja")
public class CajaController {

    private final MovimientoCajaService movimientoCajaService;
    private final CierreCajaService cierreCajaService;

    public CajaController(
            MovimientoCajaService movimientoCajaService,
            CierreCajaService cierreCajaService) {
        this.movimientoCajaService = movimientoCajaService;
        this.cierreCajaService = cierreCajaService;
    }

    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoCajaResponse> registrarManual(
            @Valid @RequestBody MovimientoCajaRequest request) {
        return ResponseEntity.status(201).body(movimientoCajaService.registrarManual(request));
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoCajaResponse>> listarMovimientos(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(movimientoCajaService.listarPorFecha(fecha));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenCajaResponse> obtenerResumen(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(movimientoCajaService.obtenerResumen(fecha));
    }

    @PatchMapping("/movimientos/{id}/anular")
    public ResponseEntity<MovimientoCajaResponse> anularManual(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoCajaService.anularManual(id));
    }

    @PostMapping("/cierres")
    public ResponseEntity<CierreCajaResponse> cerrar(
            @Valid @RequestBody CierreCajaRequest request) {
        return ResponseEntity.status(201).body(cierreCajaService.cerrar(request));
    }

    @GetMapping("/cierres")
    public ResponseEntity<List<CierreCajaResponse>> listarCierres() {
        return ResponseEntity.ok(cierreCajaService.listar());
    }

    @GetMapping("/cierres/{fecha}")
    public ResponseEntity<CierreCajaResponse> buscarCierre(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(cierreCajaService.buscarPorFecha(fecha));
    }

    @PatchMapping("/cierres/{fecha}/reabrir")
    public ResponseEntity<CierreCajaResponse> reabrir(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(cierreCajaService.reabrir(fecha));
    }
}
