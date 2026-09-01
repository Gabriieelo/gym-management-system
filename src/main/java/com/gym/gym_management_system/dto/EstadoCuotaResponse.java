package com.gym.gym_management_system.dto;

import java.time.YearMonth;

public record EstadoCuotaResponse(
        Long clienteId,
        YearMonth periodo,
        boolean cuotaPagada
) {
}
