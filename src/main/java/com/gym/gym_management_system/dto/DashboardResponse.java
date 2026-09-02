package com.gym.gym_management_system.dto;

import com.gym.gym_management_system.entity.EstadoCierreCaja;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardResponse(
        LocalDate fecha,
        long clientesActivos,
        long clientesInactivos,
        long cuotasAlDia,
        long cuotasPendientes,
        long cuotasVencidas,
        long asistenciasDelDia,
        long pagosMensualesDelMes,
        long pasesDiariosDelDia,
        BigDecimal ingresosDelDia,
        BigDecimal ingresosDelMes,
        BigDecimal efectivoDelDia,
        BigDecimal transferenciasDelDia,
        EstadoCierreCaja estadoCaja
) {
}
