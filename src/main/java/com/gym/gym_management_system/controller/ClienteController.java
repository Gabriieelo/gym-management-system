package com.gym.gym_management_system.controller;

import com.gym.gym_management_system.dto.ClienteRequest;
import com.gym.gym_management_system.dto.ClienteResponse;
import com.gym.gym_management_system.dto.ClienteBusquedaResponse;
import com.gym.gym_management_system.dto.EstadoClienteRequest;
import com.gym.gym_management_system.dto.EstadoCuotaResponse;
import com.gym.gym_management_system.dto.PaginaResponse;
import com.gym.gym_management_system.entity.EstadoCuota;
import com.gym.gym_management_system.service.ClienteBusquedaService;
import com.gym.gym_management_system.service.ClienteService;
import com.gym.gym_management_system.service.EstadoCuotaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/clientes")
@Validated
public class ClienteController {

    private final ClienteService clienteService;
    private final EstadoCuotaService estadoCuotaService;
    private final ClienteBusquedaService clienteBusquedaService;

    public ClienteController(
            ClienteService clienteService,
            EstadoCuotaService estadoCuotaService,
            ClienteBusquedaService clienteBusquedaService) {
        this.clienteService = clienteService;
        this.estadoCuotaService = estadoCuotaService;
        this.clienteBusquedaService = clienteBusquedaService;
    }

    @GetMapping("/busqueda")
    public ResponseEntity<PaginaResponse<ClienteBusquedaResponse>> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) EstadoCuota estadoCuota,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanio) {
        return ResponseEntity.ok(
                clienteBusquedaService.buscar(texto, activo, estadoCuota, pagina, tamanio));
    }

    @GetMapping("/cuotas/vencidas")
    public ResponseEntity<List<EstadoCuotaResponse>> listarCuotasVencidas() {
        return ResponseEntity.ok(estadoCuotaService.listarVencidas());
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse cliente = clienteService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cliente.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(cliente);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar(
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(clienteService.listar(activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ClienteResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoClienteRequest request) {
        return ResponseEntity.ok(clienteService.cambiarEstado(id, request.activo()));
    }
}
