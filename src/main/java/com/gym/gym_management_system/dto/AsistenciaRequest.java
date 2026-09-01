package com.gym.gym_management_system.dto;

import jakarta.validation.constraints.NotNull;

public record AsistenciaRequest(
        @NotNull(message = "El cliente es obligatorio")
        Long clienteId
) {
}
