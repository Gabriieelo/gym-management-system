package com.gym.gym_management_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumenCajaResponse(
        LocalDate fecha,
        BigDecimal totalIngresos,
        BigDecimal totalEgresos,
        BigDecimal saldo,
        long movimientosActivos,
        long movimientosAnulados
) {
}
