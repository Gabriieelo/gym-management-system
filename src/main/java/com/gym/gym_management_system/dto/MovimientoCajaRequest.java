package com.gym.gym_management_system.dto;

import com.gym.gym_management_system.entity.TipoMovimientoCaja;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record MovimientoCajaRequest(
        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimientoCaja tipo,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
        @Digits(integer = 10, fraction = 2, message = "El monto admite hasta 10 enteros y 2 decimales")
        BigDecimal monto,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
        String descripcion
) {
}
