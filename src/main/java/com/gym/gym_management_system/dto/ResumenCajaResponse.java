package com.gym.gym_management_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumenCajaResponse(
        LocalDate fecha,
        BigDecimal totalIngresos,
        BigDecimal ingresosEfectivo,
        BigDecimal ingresosTransferencia,
        BigDecimal totalEgresos,
        BigDecimal egresosEfectivo,
        BigDecimal egresosTransferencia,
        BigDecimal saldo,
        BigDecimal saldoEfectivo,
        BigDecimal saldoTransferencia,
        long movimientosActivos,
        long movimientosAnulados
) {
}
