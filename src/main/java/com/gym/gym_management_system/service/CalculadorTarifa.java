package com.gym.gym_management_system.service;

import com.gym.gym_management_system.entity.TipoPago;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class CalculadorTarifa {

    public static final BigDecimal CUOTA_EN_TERMINO = new BigDecimal("38000.00");
    public static final BigDecimal CUOTA_CON_RECARGO = new BigDecimal("42000.00");
    public static final BigDecimal PASE_DIARIO = new BigDecimal("4000.00");
    private static final int DIA_LIMITE_NORMAL = 10;

    public BigDecimal calcular(TipoPago tipo, LocalDate fechaPago) {
        if (tipo == TipoPago.PASE_DIARIO) {
            return PASE_DIARIO;
        }

        LocalDate limiteSinRecargo = calcularLimiteSinRecargo(fechaPago);
        return !fechaPago.isAfter(limiteSinRecargo)
                ? CUOTA_EN_TERMINO
                : CUOTA_CON_RECARGO;
    }

    private LocalDate calcularLimiteSinRecargo(LocalDate fechaPago) {
        LocalDate diaDiez = fechaPago.withDayOfMonth(DIA_LIMITE_NORMAL);
        return diaDiez.getDayOfWeek() == DayOfWeek.SUNDAY
                ? diaDiez.plusDays(1)
                : diaDiez;
    }
}
