package com.gym.gym_management_system.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TarifaRequest(
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2)
        BigDecimal cuotaEnTermino,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2)
        BigDecimal cuotaConRecargo,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2)
        BigDecimal paseDiario
) {
}
