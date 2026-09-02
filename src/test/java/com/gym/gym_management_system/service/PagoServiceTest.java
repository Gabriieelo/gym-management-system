package com.gym.gym_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.gym.gym_management_system.dto.PagoRequest;
import com.gym.gym_management_system.dto.PagoResponse;
import com.gym.gym_management_system.entity.Cliente;
import com.gym.gym_management_system.entity.EstadoPago;
import com.gym.gym_management_system.entity.MedioPago;
import com.gym.gym_management_system.entity.Pago;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.exception.ClienteInactivoException;
import com.gym.gym_management_system.exception.PagoDuplicadoException;
import com.gym.gym_management_system.repository.ClienteRepository;
import com.gym.gym_management_system.repository.PagoRepository;
import com.gym.gym_management_system.security.UsuarioActualService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private MovimientoCajaService movimientoCajaService;

    @Mock
    private UsuarioActualService usuarioActualService;

    @Mock
    private EstadoCuotaService estadoCuotaService;

    private PagoService pagoService;
    private Cliente cliente;

    @BeforeEach
    void configurar() {
        Clock reloj = Clock.fixed(Instant.parse("2026-09-10T15:00:00Z"), ZONA);
        pagoService = new PagoService(
                pagoRepository,
                clienteRepository,
                new CalculadorTarifa(TarifasPrueba.servicio()),
                movimientoCajaService,
                usuarioActualService,
                estadoCuotaService,
                reloj
        );

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Ana");
        cliente.setApellido("Pérez");
        cliente.setActivo(true);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
    }

    @Test
    void registraLaCuotaDelMesActual() {
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocacion -> {
            Pago pago = invocacion.getArgument(0);
            pago.setId(10L);
            return pago;
        });

        PagoResponse response = pagoService.registrar(
                new PagoRequest(1L, TipoPago.MENSUAL, MedioPago.EFECTIVO, null));

        assertEquals(new BigDecimal("38000.00"), response.monto());
        assertEquals(9, response.periodoMes());
        assertEquals(2026, response.periodoAnio());
        assertNull(response.fechaUso());
        assertEquals(EstadoPago.CONFIRMADO, response.estado());
        assertEquals(MedioPago.EFECTIVO, response.medioPago());
        verify(movimientoCajaService).registrarDesdePago(any(Pago.class));
    }

    @Test
    void registraUnPaseParaElDiaActual() {
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        PagoResponse response = pagoService.registrar(
                new PagoRequest(1L, TipoPago.PASE_DIARIO, MedioPago.TRANSFERENCIA, " Comprobante 123 "));

        assertEquals(new BigDecimal("4000.00"), response.monto());
        assertEquals(LocalDate.of(2026, 9, 10), response.fechaUso());
        assertEquals("Comprobante 123", response.observacion());
        assertEquals(MedioPago.TRANSFERENCIA, response.medioPago());
        assertNull(response.periodoMes());
    }

    @Test
    void rechazaUnaCuotaDuplicada() {
        when(pagoRepository.existsByClienteIdAndTipoAndPeriodoMesAndPeriodoAnioAndEstado(
                1L, TipoPago.MENSUAL, 9, 2026, EstadoPago.CONFIRMADO
        )).thenReturn(true);

        assertThrows(
                PagoDuplicadoException.class,
                () -> pagoService.registrar(
                        new PagoRequest(1L, TipoPago.MENSUAL, MedioPago.EFECTIVO, null))
        );
    }

    @Test
    void rechazaPagosDeClientesInactivos() {
        cliente.setActivo(false);

        assertThrows(
                ClienteInactivoException.class,
                () -> pagoService.registrar(
                        new PagoRequest(1L, TipoPago.PASE_DIARIO, MedioPago.EFECTIVO, null))
        );
    }
}
