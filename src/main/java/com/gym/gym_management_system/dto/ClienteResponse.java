package com.gym.gym_management_system.dto;

import java.time.LocalDate;

public record ClienteResponse(
        Long id,
        String nombre,
        String apellido,
        String dni,
        String telefono,
        String contactoEmergencia,
        String fotoUrl,
        LocalDate fechaIngreso,
        boolean activo
) {
}
