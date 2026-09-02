package com.gym.gym_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.gym.gym_management_system.dto.DashboardResponse;
import com.gym.gym_management_system.dto.EstadoCuotaResponse;
import com.gym.gym_management_system.dto.ResumenCajaResponse;
import com.gym.gym_management_system.entity.EstadoAsistencia;
import com.gym.gym_management_system.entity.EstadoCierreCaja;
import com.gym.gym_management_system.entity.EstadoCuota;
import com.gym.gym_management_system.entity.EstadoPago;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.repository.AsistenciaRepository;
import com.gym.gym_management_system.repository.CierreCajaRepository;
import com.gym.gym_management_system.repository.ClienteRepository;
import com.gym.gym_management_system.repository.PagoRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private AsistenciaRepository asistenciaRepository;
    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private CierreCajaRepository cierreCajaRepository;
    @Mock
    private EstadoCuotaService estadoCuotaService;
    @Mock
    private MovimientoCajaService movimientoCajaService;

    private DashboardService dashboardService;

    @BeforeEach
    void configurar() {
        Clock reloj = Clock.fixed(
                Instant.parse("2026-09-15T15:00:00Z"),
                ZoneId.of("America/Argentina/Buenos_Aires"));
        dashboardService = new DashboardService(
                clienteRepository,
                asistenciaRepository,
                pagoRepository,
                cierreCajaRepository,
                estadoCuotaService,
                movimientoCajaService,
                reloj);
    }

    @Test
    void armaElResumenGeneralConLaInformacionDeCadaModulo() {
        LocalDate hoy = LocalDate.of(2026, 9, 15);
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.plusDays(1).atStartOfDay();
        LocalDateTime inicioMes = LocalDate.of(2026, 9, 1).atStartOfDay();
        LocalDateTime finMes = LocalDate.of(2026, 10, 1).atStartOfDay();
        when(clienteRepository.countByActivo(true)).thenReturn(8L);
        when(clienteRepository.countByActivo(false)).thenReturn(2L);
        when(estadoCuotaService.listarActivos()).thenReturn(List.of(
                cuota(1L, EstadoCuota.AL_DIA),
                cuota(2L, EstadoCuota.AL_DIA),
                cuota(3L, EstadoCuota.PENDIENTE),
                cuota(4L, EstadoCuota.VENCIDA)));
        when(asistenciaRepository.countByFechaAndEstado(hoy, EstadoAsistencia.REGISTRADA))
                .thenReturn(12L);
        when(pagoRepository.countByTipoAndEstadoAndFechaPagoGreaterThanEqualAndFechaPagoLessThan(
                TipoPago.MENSUAL, EstadoPago.CONFIRMADO, inicioMes, finMes)).thenReturn(6L);
        when(pagoRepository.countByTipoAndEstadoAndFechaPagoGreaterThanEqualAndFechaPagoLessThan(
                TipoPago.PASE_DIARIO, EstadoPago.CONFIRMADO, inicioDia, finDia)).thenReturn(3L);
        when(movimientoCajaService.obtenerResumen(hoy)).thenReturn(new ResumenCajaResponse(
                hoy,
                new BigDecimal("50000.00"),
                new BigDecimal("30000.00"),
                new BigDecimal("20000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("50000.00"),
                new BigDecimal("30000.00"),
                new BigDecimal("20000.00"),
                4,
                0));
        when(movimientoCajaService.obtenerIngresosEntre(inicioMes, finMes))
                .thenReturn(new BigDecimal("350000.00"));
        when(cierreCajaRepository.findByFecha(hoy)).thenReturn(Optional.empty());

        DashboardResponse response = dashboardService.obtenerResumen();

        assertEquals(hoy, response.fecha());
        assertEquals(8L, response.clientesActivos());
        assertEquals(2L, response.clientesInactivos());
        assertEquals(2L, response.cuotasAlDia());
        assertEquals(1L, response.cuotasPendientes());
        assertEquals(1L, response.cuotasVencidas());
        assertEquals(12L, response.asistenciasDelDia());
        assertEquals(6L, response.pagosMensualesDelMes());
        assertEquals(3L, response.pasesDiariosDelDia());
        assertEquals(new BigDecimal("50000.00"), response.ingresosDelDia());
        assertEquals(new BigDecimal("350000.00"), response.ingresosDelMes());
        assertEquals(EstadoCierreCaja.ABIERTO, response.estadoCaja());
    }

    private EstadoCuotaResponse cuota(Long clienteId, EstadoCuota estado) {
        return new EstadoCuotaResponse(
                clienteId,
                "Cliente " + clienteId,
                YearMonth.of(2026, 9),
                estado == EstadoCuota.AL_DIA,
                estado,
                LocalDate.of(2026, 9, 10),
                null,
                null);
    }
}
