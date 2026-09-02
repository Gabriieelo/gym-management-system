package com.gym.gym_management_system.dto;

import com.gym.gym_management_system.entity.EstadoCuota;
import java.time.LocalDate;
import java.time.YearMonth;

public record ClienteBusquedaResponse(
        Long id,
        String nombre,
        String apellido,
        String dni,
        String telefono,
        LocalDate fechaIngreso,
        boolean activo,
        EstadoCuota estadoCuota,
        YearMonth ultimoPeriodoPagado
) {
}
