package com.gym.gym_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doReturn;

import com.gym.gym_management_system.dto.EstadoCuotaResponse;
import com.gym.gym_management_system.entity.Cliente;
import com.gym.gym_management_system.entity.EstadoCuota;
import com.gym.gym_management_system.entity.EstadoPago;
import com.gym.gym_management_system.entity.Pago;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.repository.ClienteRepository;
import com.gym.gym_management_system.repository.PagoRepository;
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
class EstadoCuotaServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PagoRepository pagoRepository;

    private Cliente cliente;

    @BeforeEach
    void configurarCliente() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Ana");
        cliente.setApellido("Pérez");
        cliente.setActivo(true);
    }

    @Test
    void informaPendienteMientrasTodaviaEstaDentroDelPlazo() {
        EstadoCuotaResponse response = servicioEn("2026-09-10T15:00:00Z").consultar(1L);

        assertEquals(EstadoCuota.PENDIENTE, response.estado());
        assertEquals(LocalDate.of(2026, 9, 10), response.fechaLimitePago());
        assertFalse(response.cuotaPagada());
    }

    @Test
    void informaVencidaDespuesDelPlazo() {
        EstadoCuotaResponse response = servicioEn("2026-09-11T15:00:00Z").consultar(1L);

        assertEquals(EstadoCuota.VENCIDA, response.estado());
    }

    @Test
    void informaAlDiaYDevuelveLosDatosDelUltimoPago() {
        Pago pago = new Pago();
        pago.setPeriodoMes(9);
        pago.setPeriodoAnio(2026);
        pago.setFechaPago(LocalDateTime.of(2026, 9, 5, 12, 30));
        when(pagoRepository.existsByClienteIdAndTipoAndPeriodoMesAndPeriodoAnioAndEstado(
                1L, TipoPago.MENSUAL, 9, 2026, EstadoPago.CONFIRMADO)).thenReturn(true);
        when(pagoRepository.findFirstByClienteIdAndTipoAndEstadoOrderByPeriodoAnioDescPeriodoMesDesc(
                1L, TipoPago.MENSUAL, EstadoPago.CONFIRMADO)).thenReturn(Optional.of(pago));

        EstadoCuotaResponse response = servicioEn("2026-09-11T15:00:00Z").consultar(1L);

        assertEquals(EstadoCuota.AL_DIA, response.estado());
        assertTrue(response.cuotaPagada());
        assertEquals(LocalDate.of(2026, 9, 5), response.fechaUltimoPago());
        assertEquals(YearMonth.of(2026, 9), response.ultimoPeriodoPagado());
    }

    @Test
    void noMarcaComoVencidaLaProrrogaDelLunes() {
        EstadoCuotaResponse response = servicioEn("2026-05-11T15:00:00Z").consultar(1L);

        assertEquals(EstadoCuota.PENDIENTE, response.estado());
        assertEquals(LocalDate.of(2026, 5, 11), response.fechaLimitePago());
    }

    @Test
    void listaSoloLosClientesActivosConCuotaVencida() {
        Cliente alDia = new Cliente();
        alDia.setId(2L);
        alDia.setNombre("Luis");
        alDia.setApellido("Gómez");
        alDia.setActivo(true);
        when(clienteRepository.findByActivo(true)).thenReturn(List.of(cliente, alDia));
        doReturn(false, true).when(pagoRepository)
                .existsByClienteIdAndTipoAndPeriodoMesAndPeriodoAnioAndEstado(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.eq(TipoPago.MENSUAL),
                        org.mockito.ArgumentMatchers.eq(9),
                        org.mockito.ArgumentMatchers.eq(2026),
                        org.mockito.ArgumentMatchers.eq(EstadoPago.CONFIRMADO));

        List<EstadoCuotaResponse> vencidas = servicioEn("2026-09-11T15:00:00Z").listarVencidas();

        assertEquals(1, vencidas.size());
        assertEquals(1L, vencidas.get(0).clienteId());
    }

    private EstadoCuotaService servicioEn(String instante) {
        lenient().when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        Clock reloj = Clock.fixed(Instant.parse(instante), ZONA);
        return new EstadoCuotaService(
                clienteRepository, pagoRepository, new CalculadorTarifa(), reloj);
    }
}
