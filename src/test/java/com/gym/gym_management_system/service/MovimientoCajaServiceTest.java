package com.gym.gym_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gym.gym_management_system.dto.MovimientoCajaRequest;
import com.gym.gym_management_system.dto.MovimientoCajaResponse;
import com.gym.gym_management_system.dto.ResumenCajaResponse;
import com.gym.gym_management_system.entity.Cliente;
import com.gym.gym_management_system.entity.EstadoMovimientoCaja;
import com.gym.gym_management_system.entity.MovimientoCaja;
import com.gym.gym_management_system.entity.MedioPago;
import com.gym.gym_management_system.entity.OrigenMovimientoCaja;
import com.gym.gym_management_system.entity.Pago;
import com.gym.gym_management_system.entity.TipoMovimientoCaja;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.exception.OperacionCajaInvalidaException;
import com.gym.gym_management_system.exception.CajaYaCerradaException;
import com.gym.gym_management_system.entity.EstadoCierreCaja;
import com.gym.gym_management_system.repository.CierreCajaRepository;
import com.gym.gym_management_system.security.UsuarioActualService;
import com.gym.gym_management_system.repository.MovimientoCajaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MovimientoCajaServiceTest {

    @Mock
    private MovimientoCajaRepository movimientoRepository;

    @Mock
    private CierreCajaRepository cierreRepository;

    @Mock
    private UsuarioActualService usuarioActualService;

    private MovimientoCajaService movimientoService;

    @BeforeEach
    void configurar() {
        Clock reloj = Clock.fixed(
                Instant.parse("2026-09-15T15:00:00Z"),
                ZoneId.of("America/Argentina/Buenos_Aires")
        );
        movimientoService = new MovimientoCajaService(
                movimientoRepository, cierreRepository, usuarioActualService, reloj);
    }

    @Test
    void registraUnEgresoManual() {
        when(movimientoRepository.save(any(MovimientoCaja.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        MovimientoCajaResponse response = movimientoService.registrarManual(
                new MovimientoCajaRequest(
                        TipoMovimientoCaja.EGRESO,
                        new BigDecimal("5000.00"),
                        MedioPago.EFECTIVO,
                        " Compra de productos de limpieza "
                )
        );

        assertEquals(OrigenMovimientoCaja.MANUAL, response.origen());
        assertEquals(EstadoMovimientoCaja.ACTIVO, response.estado());
        assertEquals(MedioPago.EFECTIVO, response.medioPago());
        assertEquals("Compra de productos de limpieza", response.descripcion());
        assertNull(response.pagoId());
    }

    @Test
    void generaUnIngresoVinculadoAlPago() {
        Pago pago = crearPago();

        movimientoService.registrarDesdePago(pago);

        verify(movimientoRepository).save(any(MovimientoCaja.class));
    }

    @Test
    void calculaElSaldoIgnorandoMovimientosAnulados() {
        LocalDate fecha = LocalDate.of(2026, 9, 15);
        when(movimientoRepository
                .findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAsc(
                        fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay()
                ))
                .thenReturn(List.of(
                        crearMovimiento(
                                TipoMovimientoCaja.INGRESO, "38000.00",
                                MedioPago.EFECTIVO, EstadoMovimientoCaja.ACTIVO),
                        crearMovimiento(
                                TipoMovimientoCaja.EGRESO, "5000.00",
                                MedioPago.TRANSFERENCIA, EstadoMovimientoCaja.ACTIVO),
                        crearMovimiento(
                                TipoMovimientoCaja.INGRESO, "4000.00",
                                MedioPago.TRANSFERENCIA, EstadoMovimientoCaja.ANULADO)
                ));

        ResumenCajaResponse resumen = movimientoService.obtenerResumen(fecha);

        assertEquals(new BigDecimal("38000.00"), resumen.totalIngresos());
        assertEquals(new BigDecimal("5000.00"), resumen.totalEgresos());
        assertEquals(new BigDecimal("33000.00"), resumen.saldo());
        assertEquals(new BigDecimal("38000.00"), resumen.ingresosEfectivo());
        assertEquals(BigDecimal.ZERO, resumen.ingresosTransferencia());
        assertEquals(BigDecimal.ZERO, resumen.egresosEfectivo());
        assertEquals(new BigDecimal("5000.00"), resumen.egresosTransferencia());
        assertEquals(new BigDecimal("38000.00"), resumen.saldoEfectivo());
        assertEquals(new BigDecimal("-5000.00"), resumen.saldoTransferencia());
        assertEquals(2, resumen.movimientosActivos());
        assertEquals(1, resumen.movimientosAnulados());
    }

    @Test
    void impideAnularDesdeCajaUnMovimientoOriginadoPorPago() {
        MovimientoCaja movimiento = crearMovimiento(
                TipoMovimientoCaja.INGRESO,
                "38000.00",
                MedioPago.EFECTIVO,
                EstadoMovimientoCaja.ACTIVO
        );
        movimiento.setOrigen(OrigenMovimientoCaja.PAGO);
        when(movimientoRepository.findById(1L)).thenReturn(Optional.of(movimiento));

        assertThrows(OperacionCajaInvalidaException.class, () -> movimientoService.anularManual(1L));
    }

    @Test
    void impideRegistrarMovimientosCuandoLaCajaEstaCerrada() {
        LocalDate fecha = LocalDate.of(2026, 9, 15);
        when(cierreRepository.existsByFechaAndEstado(fecha, EstadoCierreCaja.CERRADO))
                .thenReturn(true);

        assertThrows(
                CajaYaCerradaException.class,
                () -> movimientoService.registrarManual(new MovimientoCajaRequest(
                        TipoMovimientoCaja.INGRESO,
                        new BigDecimal("1000.00"),
                        MedioPago.EFECTIVO,
                        "Ingreso posterior al cierre"
                ))
        );
    }

    private Pago crearPago() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Ana");
        cliente.setApellido("Pérez");

        Pago pago = new Pago();
        pago.setId(10L);
        pago.setCliente(cliente);
        pago.setTipo(TipoPago.MENSUAL);
        pago.setMonto(new BigDecimal("42000.00"));
        pago.setMedioPago(MedioPago.TRANSFERENCIA);
        pago.setFechaPago(LocalDateTime.of(2026, 9, 15, 12, 0));
        pago.setPeriodoMes(9);
        pago.setPeriodoAnio(2026);
        return pago;
    }

    private MovimientoCaja crearMovimiento(
            TipoMovimientoCaja tipo,
            String monto,
            MedioPago medioPago,
            EstadoMovimientoCaja estado) {
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setId(1L);
        movimiento.setTipo(tipo);
        movimiento.setOrigen(OrigenMovimientoCaja.MANUAL);
        movimiento.setEstado(estado);
        movimiento.setMonto(new BigDecimal(monto));
        movimiento.setMedioPago(medioPago);
        movimiento.setFechaHora(LocalDateTime.of(2026, 9, 15, 12, 0));
        movimiento.setDescripcion("Movimiento de prueba");
        return movimiento;
    }
}
