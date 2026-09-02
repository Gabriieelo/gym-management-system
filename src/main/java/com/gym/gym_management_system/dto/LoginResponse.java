package com.gym.gym_management_system.dto;

import com.gym.gym_management_system.entity.RolUsuario;
import java.time.Instant;

public record LoginResponse(
        String token,
        String tipo,
        Instant expiraEn,
        Long usuarioId,
        String nombreUsuario,
        String nombreCompleto,
        RolUsuario rol
) {
}
