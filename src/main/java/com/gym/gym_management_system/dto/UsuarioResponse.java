package com.gym.gym_management_system.dto;

import com.gym.gym_management_system.entity.RolUsuario;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nombreCompleto,
        String nombreUsuario,
        RolUsuario rol,
        boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
