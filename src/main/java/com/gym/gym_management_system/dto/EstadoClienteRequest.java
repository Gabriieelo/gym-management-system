package com.gym.gym_management_system.dto;

import jakarta.validation.constraints.NotNull;

public record EstadoClienteRequest(
        @NotNull(message = "El estado es obligatorio")
        Boolean activo
) {
}
