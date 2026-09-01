package com.gym.gym_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gym.gym_management_system.dto.CierreCajaRequest;
import com.gym.gym_management_system.dto.CierreCajaResponse;
import com.gym.gym_management_system.dto.ResumenCajaResponse;
import com.gym.gym_management_system.entity.CierreCaja;
import com.gym.gym_management_system.entity.EstadoCierreCaja;
import com.gym.gym_management_system.exception.CajaYaCerradaException;
import com.gym.gym_management_system.repository.CierreCajaRepository;
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
class CierreCajaServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 9, 15);

    @Mock
    private CierreCajaRepository cierreRepository;

    @Mock
    private MovimientoCajaService movimientoCajaService;

    private CierreCajaService cierreService;

    @BeforeEach
    void configurar() {
        Clock reloj = Clock.fixed(
                Instant.parse("2026-09-15T23:00:00Z"),
                ZoneId.of("America/Argentina/Buenos_Aires")
        );
        cierreService = new CierreCajaService(cierreRepository, movimientoCajaService, reloj);
    }

    @Test
    void cierraLaCajaSinDiferencias() {
        prepararNuevoCierre();

        CierreCajaResponse response = cierreService.cerrar(new CierreCajaRequest(
                FECHA,
                new BigDecimal("33000.00"),
                new BigDecimal("12000.00"),
                " Todo correcto "
        ));

        assertEquals(EstadoCierreCaja.CERRADO, response.estado());
        assertEquals(new BigDecimal("33000.00"), response.efectivoEsperado());
        assertEquals(new BigDecimal("12000.00"), response.transferenciasEsperadas());
        assertEquals(BigDecimal.ZERO.setScale(2), response.diferenciaEfectivo());
        assertTrue(response.efectivoCoincide());
        assertTrue(response.transferenciasCoinciden());
        assertEquals("Todo correcto", response.observacion());
    }

    @Test
    void informaFaltanteDeEfectivoYDiferenciaEnTransferencias() {
        prepararNuevoCierre();

        CierreCajaResponse response = cierreService.cerrar(new CierreCajaRequest(
                FECHA,
                new BigDecimal("32000.00"),
                new BigDecimal("12500.00"),
                null
        ));

        assertEquals(new BigDecimal("-1000.00"), response.diferenciaEfectivo());
        assertEquals(new BigDecimal("500.00"), response.diferenciaTransferencias());
        assertFalse(response.efectivoCoincide());
        assertFalse(response.transferenciasCoinciden());
    }

    @Test
    void impideCerrarDosVecesSinReabrir() {
        CierreCaja existente = new CierreCaja();
        existente.setFecha(FECHA);
        existente.setEstado(EstadoCierreCaja.CERRADO);
        when(cierreRepository.findByFecha(FECHA)).thenReturn(Optional.of(existente));

        assertThrows(
                CajaYaCerradaException.class,
                () -> cierreService.cerrar(new CierreCajaRequest(
                        FECHA, BigDecimal.ZERO, BigDecimal.ZERO, null))
        );
    }

    @Test
    void permiteReabrirUnaCajaCerrada() {
        CierreCaja existente = cierreExistente();
        when(cierreRepository.findByFecha(FECHA)).thenReturn(Optional.of(existente));
        when(cierreRepository.save(existente)).thenReturn(existente);

        CierreCajaResponse response = cierreService.reabrir(FECHA);

        assertEquals(EstadoCierreCaja.ABIERTO, response.estado());
    }

    private void prepararNuevoCierre() {
        when(cierreRepository.findByFecha(FECHA)).thenReturn(Optional.empty());
        when(movimientoCajaService.obtenerResumen(FECHA)).thenReturn(resumen());
        when(cierreRepository.save(any(CierreCaja.class))).thenAnswer(invocacion -> {
            CierreCaja cierre = invocacion.getArgument(0);
            cierre.setId(1L);
            return cierre;
        });
    }

    private ResumenCajaResponse resumen() {
        return new ResumenCajaResponse(
                FECHA,
                new BigDecimal("50000.00"),
                new BigDecimal("38000.00"),
                new BigDecimal("12000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("5000.00"),
                BigDecimal.ZERO,
                new BigDecimal("45000.00"),
                new BigDecimal("33000.00"),
                new BigDecimal("12000.00"),
                3,
                0
        );
    }

    private CierreCaja cierreExistente() {
        CierreCaja cierre = new CierreCaja();
        cierre.setId(1L);
        cierre.setFecha(FECHA);
        cierre.setEstado(EstadoCierreCaja.CERRADO);
        cierre.setTotalIngresos(new BigDecimal("50000.00"));
        cierre.setTotalEgresos(new BigDecimal("5000.00"));
        cierre.setSaldoGeneral(new BigDecimal("45000.00"));
        cierre.setEfectivoEsperado(new BigDecimal("33000.00"));
        cierre.setEfectivoContado(new BigDecimal("33000.00"));
        cierre.setDiferenciaEfectivo(BigDecimal.ZERO);
        cierre.setTransferenciasEsperadas(new BigDecimal("12000.00"));
        cierre.setTransferenciasVerificadas(new BigDecimal("12000.00"));
        cierre.setDiferenciaTransferencias(BigDecimal.ZERO);
        return cierre;
    }
}
