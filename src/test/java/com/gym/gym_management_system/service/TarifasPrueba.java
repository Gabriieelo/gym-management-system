package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.TarifaResponse;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;

final class TarifasPrueba {
    private TarifasPrueba() {}

    static TarifaService servicio() {
        TarifaService servicio = mock(TarifaService.class);
        lenient().when(servicio.consultar()).thenReturn(new TarifaResponse(
                new BigDecimal("38000.00"),
                new BigDecimal("42000.00"),
                new BigDecimal("4000.00")));
        return servicio;
    }
}
