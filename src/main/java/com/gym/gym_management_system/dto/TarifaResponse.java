package com.gym.gym_management_system.dto;

import java.math.BigDecimal;

public record TarifaResponse(
        BigDecimal cuotaEnTermino,
        BigDecimal cuotaConRecargo,
        BigDecimal paseDiario
) {
}
