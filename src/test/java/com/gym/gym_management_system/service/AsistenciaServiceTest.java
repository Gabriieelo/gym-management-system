package com.gym.gym_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gym.gym_management_system.dto.AsistenciaRequest;
import com.gym.gym_management_system.dto.AsistenciaResponse;
import com.gym.gym_management_system.entity.Asistencia;
import com.gym.gym_management_system.entity.Cliente;
import com.gym.gym_management_system.entity.EstadoAsistencia;
import com.gym.gym_management_system.entity.EstadoPago;
import com.gym.gym_management_system.entity.ModalidadAcceso;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.exception.AccesoNoHabilitadoException;
import com.gym.gym_management_system.exception.AsistenciaDuplicadaException;
import com.gym.gym_management_system.exception.ClienteInactivoException;
import com.gym.gym_management_system.repository.AsistenciaRepository;
import com.gym.gym_management_system.repository.ClienteRepository;
import com.gym.gym_management_system.repository.PagoRepository;
import com.gym.gym_management_system.security.UsuarioActualService;
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
class AsistenciaServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 9, 15);

    @Mock
    private AsistenciaRepository asistenciaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private UsuarioActualService usuarioActualService;

    private AsistenciaService asistenciaService;
    private Cliente cliente;

    @BeforeEach
    void configurar() {
        Clock reloj = Clock.fixed(
                Instant.parse("2026-09-15T15:00:00Z"),
                ZoneId.of("America/Argentina/Buenos_Aires")
        );
        asistenciaService = new AsistenciaService(
                asistenciaRepository,
                clienteRepository,
                pagoRepository,
                usuarioActualService,
                reloj
        );

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Ana");
        cliente.setApellido("Pérez");
        cliente.setActivo(true);
    }

    @Test
    void registraLaAsistenciaConCuotaMensual() {
        prepararCliente();
        habilitarCuotaMensual();
        prepararGuardado();

        AsistenciaResponse response = asistenciaService.registrar(new AsistenciaRequest(1L));

        assertEquals(ModalidadAcceso.MENSUAL, response.modalidad());
        assertEquals(EstadoAsistencia.REGISTRADA, response.estado());
        assertEquals(FECHA, response.fecha());
    }

    @Test
    void registraLaAsistenciaConPaseDiario() {
        prepararCliente();
        when(pagoRepository.existsByClienteIdAndTipoAndFechaUsoAndEstado(
                1L, TipoPago.PASE_DIARIO, FECHA, EstadoPago.CONFIRMADO
        )).thenReturn(true);
        prepararGuardado();

        AsistenciaResponse response = asistenciaService.registrar(new AsistenciaRequest(1L));

        assertEquals(ModalidadAcceso.PASE_DIARIO, response.modalidad());
    }

    @Test
    void rechazaElIngresoSinPagoVigente() {
        prepararCliente();
        assertThrows(
                AccesoNoHabilitadoException.class,
                () -> asistenciaService.registrar(new AsistenciaRequest(1L))
        );
    }

    @Test
    void rechazaUnaSegundaAsistenciaElMismoDia() {
        prepararCliente();
        when(asistenciaRepository.existsByClienteIdAndFechaAndEstado(
                1L, FECHA, EstadoAsistencia.REGISTRADA
        )).thenReturn(true);

        assertThrows(
                AsistenciaDuplicadaException.class,
                () -> asistenciaService.registrar(new AsistenciaRequest(1L))
        );
    }

    @Test
    void rechazaElIngresoDeUnClienteInactivo() {
        cliente.setActivo(false);
        prepararCliente();

        assertThrows(
                ClienteInactivoException.class,
                () -> asistenciaService.registrar(new AsistenciaRequest(1L))
        );
    }

    @Test
    void anulaUnaAsistenciaSinEliminarla() {
        Asistencia asistencia = new Asistencia();
        asistencia.setId(20L);
        asistencia.setCliente(cliente);
        asistencia.setFecha(FECHA);
        asistencia.setFechaHora(FECHA.atTime(12, 0));
        asistencia.setModalidad(ModalidadAcceso.MENSUAL);
        asistencia.setEstado(EstadoAsistencia.REGISTRADA);
        when(asistenciaRepository.findById(20L)).thenReturn(Optional.of(asistencia));
        when(asistenciaRepository.save(asistencia)).thenReturn(asistencia);

        AsistenciaResponse response = asistenciaService.anular(20L);

        assertEquals(EstadoAsistencia.ANULADA, response.estado());
    }

    private void habilitarCuotaMensual() {
        when(pagoRepository.existsByClienteIdAndTipoAndPeriodoMesAndPeriodoAnioAndEstado(
                1L, TipoPago.MENSUAL, 9, 2026, EstadoPago.CONFIRMADO
        )).thenReturn(true);
    }

    private void prepararCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
    }

    private void prepararGuardado() {
        when(asistenciaRepository.save(any(Asistencia.class))).thenAnswer(invocacion -> {
            Asistencia asistencia = invocacion.getArgument(0);
            asistencia.setId(20L);
            return asistencia;
        });
    }
}
