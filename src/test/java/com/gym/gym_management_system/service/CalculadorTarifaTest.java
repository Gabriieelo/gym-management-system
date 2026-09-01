package com.gym.gym_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gym.gym_management_system.entity.TipoPago;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CalculadorTarifaTest {

    private final CalculadorTarifa calculador = new CalculadorTarifa();

    @Test
    void cobraCuotaNormalHastaElDiaDiezInclusive() {
        BigDecimal monto = calculador.calcular(TipoPago.MENSUAL, LocalDate.of(2026, 9, 10));

        assertEquals(new BigDecimal("38000.00"), monto);
    }

    @Test
    void cobraCuotaConRecargoDesdeElDiaOnce() {
        BigDecimal monto = calculador.calcular(TipoPago.MENSUAL, LocalDate.of(2026, 9, 11));

        assertEquals(new BigDecimal("42000.00"), monto);
    }

    @Test
    void cobraElValorDelPaseDiario() {
        BigDecimal monto = calculador.calcular(TipoPago.PASE_DIARIO, LocalDate.of(2026, 9, 20));

        assertEquals(new BigDecimal("4000.00"), monto);
    }
}
