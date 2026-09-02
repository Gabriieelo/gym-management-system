package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.TarifaRequest;
import com.gym.gym_management_system.entity.Tarifa;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.repository.TarifaRepository;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TarifaServiceTest {
    private final TarifaRepository repository = mock(TarifaRepository.class);
    private final TarifaService servicio = new TarifaService(repository);

    @Test
    void inicializaLosImportesActuales() {
        servicio.inicializar();
        var captor = org.mockito.ArgumentCaptor.forClass(Tarifa.class);
        verify(repository).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(new BigDecimal("38000.00"), captor.getValue().getCuotaEnTermino());
        assertEquals(new BigDecimal("42000.00"), captor.getValue().getCuotaConRecargo());
        assertEquals(new BigDecimal("4000.00"), captor.getValue().getPaseDiario());
    }

    @Test
    void noSobrescribeLaConfiguracionExistenteAlIniciar() {
        when(repository.existsById(1L)).thenReturn(true);
        servicio.inicializar();
        verify(repository, never()).save(any());
    }

    @Test
    void elCalculadorUsaLosPreciosActualizadosSinCambiarResultadosAnteriores() {
        Tarifa tarifa = new Tarifa();
        tarifa.setCuotaEnTermino(new BigDecimal("38000.00"));
        tarifa.setCuotaConRecargo(new BigDecimal("42000.00"));
        tarifa.setPaseDiario(new BigDecimal("4000.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(tarifa));
        when(repository.save(tarifa)).thenReturn(tarifa);
        CalculadorTarifa calculador = new CalculadorTarifa(servicio);
        BigDecimal montoAnterior = calculador.calcular(TipoPago.MENSUAL, LocalDate.of(2026, 9, 5));

        var response = servicio.actualizar(new TarifaRequest(
                new BigDecimal("45000.00"), new BigDecimal("50000.00"), new BigDecimal("5000.00")));

        assertEquals(new BigDecimal("45000.00"), response.cuotaEnTermino());
        assertEquals(new BigDecimal("38000.00"), montoAnterior);
        assertEquals(new BigDecimal("45000.00"),
                calculador.calcular(TipoPago.MENSUAL, LocalDate.of(2026, 9, 10)));
        assertEquals(new BigDecimal("50000.00"),
                calculador.calcular(TipoPago.MENSUAL, LocalDate.of(2026, 9, 11)));
        assertEquals(new BigDecimal("5000.00"),
                calculador.calcular(TipoPago.PASE_DIARIO, LocalDate.of(2026, 9, 11)));
        verify(repository).save(tarifa);
    }

    @Test
    void validaImportesObligatoriosPositivosYDeHastaDosDecimales() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (BigDecimal invalido : new BigDecimal[]{
                    null, BigDecimal.ZERO, new BigDecimal("-1"),
                    new BigDecimal("1.001"), new BigDecimal("10000000000")}) {
                assertFalse(validator.validate(new TarifaRequest(invalido, invalido, invalido)).isEmpty());
            }
            assertTrue(validator.validate(new TarifaRequest(
                    new BigDecimal("38000.00"), new BigDecimal("42000.00"),
                    new BigDecimal("4000.00"))).isEmpty());
        }
    }
}
