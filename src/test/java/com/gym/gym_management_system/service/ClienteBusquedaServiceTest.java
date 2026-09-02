package com.gym.gym_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gym.gym_management_system.dto.EstadoCuotaResponse;
import com.gym.gym_management_system.dto.PaginaResponse;
import com.gym.gym_management_system.dto.ClienteBusquedaResponse;
import com.gym.gym_management_system.entity.Cliente;
import com.gym.gym_management_system.entity.EstadoCuota;
import com.gym.gym_management_system.repository.ClienteRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ClienteBusquedaServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EstadoCuotaService estadoCuotaService;

    private ClienteBusquedaService busquedaService;

    @BeforeEach
    void configurar() {
        Clock reloj = Clock.fixed(
                Instant.parse("2026-09-15T15:00:00Z"),
                ZoneId.of("America/Argentina/Buenos_Aires"));
        busquedaService = new ClienteBusquedaService(
                clienteRepository, estadoCuotaService, new CalculadorTarifa(TarifasPrueba.servicio()), reloj);
    }

    @Test
    @SuppressWarnings("unchecked")
    void devuelveClientesPaginadosYOrdenadosConSuEstadoDeCuota() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Ana");
        cliente.setApellido("Pérez");
        cliente.setDni("30111222");
        cliente.setTelefono("1122334455");
        cliente.setFechaIngreso(LocalDate.of(2026, 8, 1));
        cliente.setActivo(true);

        PageRequest paginaSolicitada = PageRequest.of(1, 5);
        when(clienteRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cliente), paginaSolicitada, 8));
        when(estadoCuotaService.calcularEstado(cliente)).thenReturn(new EstadoCuotaResponse(
                1L,
                "Ana Pérez",
                YearMonth.of(2026, 9),
                true,
                EstadoCuota.AL_DIA,
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 5),
                YearMonth.of(2026, 9)));

        PaginaResponse<ClienteBusquedaResponse> response =
                busquedaService.buscar("ana", true, EstadoCuota.AL_DIA, 1, 5);

        assertEquals(1, response.pagina());
        assertEquals(5, response.tamanio());
        assertEquals(6, response.totalElementos());
        assertEquals(2, response.totalPaginas());
        assertEquals("Ana", response.contenido().get(0).nombre());
        assertEquals(EstadoCuota.AL_DIA, response.contenido().get(0).estadoCuota());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(clienteRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertEquals("apellido: ASC,nombre: ASC", pageable.getSort().toString());
    }
}
