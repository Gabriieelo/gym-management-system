package com.gym.gym_management_system.dto;

import com.gym.gym_management_system.entity.EstadoCuota;
import java.time.LocalDate;
import java.time.YearMonth;

public record EstadoCuotaResponse(
        Long clienteId,
        String nombreCompleto,
        YearMonth periodo,
        boolean cuotaPagada,
        EstadoCuota estado,
        LocalDate fechaLimitePago,
        LocalDate fechaUltimoPago,
        YearMonth ultimoPeriodoPagado
) {
}
