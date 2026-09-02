package com.gym.gym_management_system.service;

import com.gym.gym_management_system.entity.TipoPago;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class CalculadorTarifa {

    private final TarifaService tarifaService;

    public CalculadorTarifa(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }
    private static final int DIA_LIMITE_NORMAL = 10;

    public BigDecimal calcular(TipoPago tipo, LocalDate fechaPago) {
        var tarifas = tarifaService.consultar();
        if (tipo == TipoPago.PASE_DIARIO) {
            return tarifas.paseDiario();
        }

        LocalDate limiteSinRecargo = calcularLimiteSinRecargo(fechaPago);
        return !fechaPago.isAfter(limiteSinRecargo)
                ? tarifas.cuotaEnTermino()
                : tarifas.cuotaConRecargo();
    }

    public LocalDate calcularLimiteSinRecargo(LocalDate fecha) {
        LocalDate diaDiez = fecha.withDayOfMonth(DIA_LIMITE_NORMAL);
        return diaDiez.getDayOfWeek() == DayOfWeek.SUNDAY
                ? diaDiez.plusDays(1)
                : diaDiez;
    }
}
