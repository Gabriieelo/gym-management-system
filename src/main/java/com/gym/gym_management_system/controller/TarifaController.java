package com.gym.gym_management_system.controller;

import com.gym.gym_management_system.dto.TarifaRequest;
import com.gym.gym_management_system.dto.TarifaResponse;
import com.gym.gym_management_system.service.TarifaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tarifas")
public class TarifaController {
    private final TarifaService servicio;

    public TarifaController(TarifaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public TarifaResponse consultar() {
        return servicio.consultar();
    }

    @PutMapping
    public TarifaResponse actualizar(@Valid @RequestBody TarifaRequest request) {
        return servicio.actualizar(request);
    }
}
