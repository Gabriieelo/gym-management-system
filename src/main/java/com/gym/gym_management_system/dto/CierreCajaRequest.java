package com.gym.gym_management_system.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CierreCajaRequest(
        LocalDate fecha,

        @NotNull(message = "El efectivo contado es obligatorio")
        @DecimalMin(value = "0.00", message = "El efectivo contado no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El efectivo admite hasta 10 enteros y 2 decimales")
        BigDecimal efectivoContado,

        @NotNull(message = "Las transferencias verificadas son obligatorias")
        @DecimalMin(value = "0.00", message = "Las transferencias verificadas no pueden ser negativas")
        @Digits(integer = 10, fraction = 2, message = "Las transferencias admiten hasta 10 enteros y 2 decimales")
        BigDecimal transferenciasVerificadas,

        @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
        String observacion
) {
}
