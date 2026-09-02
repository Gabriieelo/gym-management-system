package com.gym.gym_management_system.controller;

import com.gym.gym_management_system.dto.CambiarPasswordRequest;
import com.gym.gym_management_system.dto.EstadoUsuarioRequest;
import com.gym.gym_management_system.dto.RestablecerPasswordRequest;
import com.gym.gym_management_system.dto.UsuarioActualizarRequest;
import com.gym.gym_management_system.dto.UsuarioRequest;
import com.gym.gym_management_system.dto.UsuarioResponse;
import com.gym.gym_management_system.service.UsuarioService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(201).body(usuarioService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioActualizarRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, request));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> restablecerPassword(
            @PathVariable Long id,
            @Valid @RequestBody RestablecerPasswordRequest request) {
        usuarioService.restablecerPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obtenerPerfil(Principal principal) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(principal.getName()));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> cambiarMiPassword(
            Principal principal,
            @Valid @RequestBody CambiarPasswordRequest request) {
        usuarioService.cambiarMiPassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
