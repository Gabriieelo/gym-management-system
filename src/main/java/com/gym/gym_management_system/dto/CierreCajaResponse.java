package com.gym.gym_management_system.dto;

import com.gym.gym_management_system.entity.EstadoCierreCaja;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CierreCajaResponse(
        Long id,
        LocalDate fecha,
        EstadoCierreCaja estado,
        BigDecimal totalIngresos,
        BigDecimal totalEgresos,
        BigDecimal saldoGeneral,
        BigDecimal efectivoEsperado,
        BigDecimal efectivoContado,
        BigDecimal diferenciaEfectivo,
        BigDecimal transferenciasEsperadas,
        BigDecimal transferenciasVerificadas,
        BigDecimal diferenciaTransferencias,
        boolean efectivoCoincide,
        boolean transferenciasCoinciden,
        LocalDateTime fechaHoraCierre,
        LocalDateTime fechaHoraReapertura,
        String observacion,
        String cerradoPor,
        String reabiertoPor
) {
}
