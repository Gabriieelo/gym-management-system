package com.gym.gym_management_system.dto;

import com.gym.gym_management_system.entity.EstadoMovimientoCaja;
import com.gym.gym_management_system.entity.OrigenMovimientoCaja;
import com.gym.gym_management_system.entity.TipoMovimientoCaja;
import com.gym.gym_management_system.entity.MedioPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoCajaResponse(
        Long id,
        TipoMovimientoCaja tipo,
        OrigenMovimientoCaja origen,
        EstadoMovimientoCaja estado,
        BigDecimal monto,
        MedioPago medioPago,
        LocalDateTime fechaHora,
        String descripcion,
        Long pagoId,
        Long clienteId,
        String cliente,
        String registradoPor,
        String anuladoPor
) {
}
