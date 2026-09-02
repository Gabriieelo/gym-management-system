package com.gym.gym_management_system.dto;

import com.gym.gym_management_system.entity.EstadoPago;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.entity.MedioPago;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PagoResponse(
        Long id,
        Long clienteId,
        String cliente,
        TipoPago tipo,
        EstadoPago estado,
        BigDecimal monto,
        MedioPago medioPago,
        LocalDateTime fechaPago,
        Integer periodoMes,
        Integer periodoAnio,
        LocalDate fechaUso,
        String observacion,
        String registradoPor,
        String anuladoPor
) {
}
