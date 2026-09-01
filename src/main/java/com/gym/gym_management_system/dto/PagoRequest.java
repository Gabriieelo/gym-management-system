package com.gym.gym_management_system.dto;

import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.entity.MedioPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PagoRequest(
        @NotNull(message = "El cliente es obligatorio")
        Long clienteId,

        @NotNull(message = "El tipo de pago es obligatorio")
        TipoPago tipo,

        @NotNull(message = "El medio de pago es obligatorio")
        MedioPago medioPago,

        @Size(max = 300, message = "La observación no puede superar los 300 caracteres")
        String observacion
) {
}
