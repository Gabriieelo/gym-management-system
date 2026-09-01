package com.gym.gym_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gym.gym_management_system.dto.ClienteRequest;
import com.gym.gym_management_system.dto.ClienteResponse;
import com.gym.gym_management_system.entity.Cliente;
import com.gym.gym_management_system.exception.ClienteNoEncontradoException;
import com.gym.gym_management_system.exception.DniDuplicadoException;
import com.gym.gym_management_system.repository.ClienteRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    private ClienteService clienteService;

    @BeforeEach
    void configurar() {
        clienteService = new ClienteService(clienteRepository);
    }

    @Test
    void creaUnClienteActivoConLosDatosNormalizados() {
        ClienteRequest request = requestValido();
        when(clienteRepository.existsByDni("30111222")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacion -> {
            Cliente cliente = invocacion.getArgument(0);
            cliente.setId(1L);
            cliente.setActivo(true);
            return cliente;
        });

        ClienteResponse response = clienteService.crear(request);

        assertEquals(1L, response.id());
        assertEquals("Ana", response.nombre());
        assertEquals("30111222", response.dni());
        assertTrue(response.activo());
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void rechazaUnDniDuplicado() {
        ClienteRequest request = requestValido();
        when(clienteRepository.existsByDni("30111222")).thenReturn(true);

        assertThrows(DniDuplicadoException.class, () -> clienteService.crear(request));
    }

    @Test
    void informaCuandoElClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNoEncontradoException.class, () -> clienteService.buscarPorId(99L));
    }

    @Test
    void permiteDarDeBajaLogicamenteUnCliente() {
        Cliente cliente = clienteExistente();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        ClienteResponse response = clienteService.cambiarEstado(1L, false);

        assertEquals(false, response.activo());
        verify(clienteRepository).save(cliente);
    }

    private ClienteRequest requestValido() {
        return new ClienteRequest(
                " Ana ",
                " Pérez ",
                "30111222",
                "1122334455",
                "María - 1199887766",
                null,
                LocalDate.of(2026, 8, 31)
        );
    }

    private Cliente clienteExistente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Ana");
        cliente.setApellido("Pérez");
        cliente.setDni("30111222");
        cliente.setFechaIngreso(LocalDate.of(2026, 8, 31));
        cliente.setActivo(true);
        return cliente;
    }
}
