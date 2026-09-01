package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.ClienteRequest;
import com.gym.gym_management_system.dto.ClienteResponse;
import com.gym.gym_management_system.entity.Cliente;
import com.gym.gym_management_system.exception.ClienteNoEncontradoException;
import com.gym.gym_management_system.exception.DniDuplicadoException;
import com.gym.gym_management_system.repository.ClienteRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ClienteResponse crear(ClienteRequest request) {
        String dni = request.dni().trim();
        if (clienteRepository.existsByDni(dni)) {
            throw new DniDuplicadoException(dni);
        }

        Cliente cliente = new Cliente();
        copiarDatos(request, cliente);
        return convertirAResponse(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(Boolean activo) {
        List<Cliente> clientes = activo == null
                ? clienteRepository.findAll()
                : clienteRepository.findByActivo(activo);

        return clientes.stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return convertirAResponse(buscarEntidad(id));
    }

    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarEntidad(id);
        String nuevoDni = request.dni().trim();

        clienteRepository.findByDni(nuevoDni)
                .filter(encontrado -> !encontrado.getId().equals(id))
                .ifPresent(encontrado -> {
                    throw new DniDuplicadoException(nuevoDni);
                });

        copiarDatos(request, cliente);
        return convertirAResponse(clienteRepository.save(cliente));
    }

    public ClienteResponse cambiarEstado(Long id, boolean activo) {
        Cliente cliente = buscarEntidad(id);
        cliente.setActivo(activo);
        return convertirAResponse(clienteRepository.save(cliente));
    }

    private Cliente buscarEntidad(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    private void copiarDatos(ClienteRequest request, Cliente cliente) {
        cliente.setNombre(request.nombre().trim());
        cliente.setApellido(request.apellido().trim());
        cliente.setDni(request.dni().trim());
        cliente.setTelefono(limpiarTextoOpcional(request.telefono()));
        cliente.setContactoEmergencia(limpiarTextoOpcional(request.contactoEmergencia()));
        cliente.setFotoUrl(limpiarTextoOpcional(request.fotoUrl()));
        cliente.setFechaIngreso(request.fechaIngreso() != null ? request.fechaIngreso() : LocalDate.now());
    }

    private String limpiarTextoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private ClienteResponse convertirAResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDni(),
                cliente.getTelefono(),
                cliente.getContactoEmergencia(),
                cliente.getFotoUrl(),
                cliente.getFechaIngreso(),
                cliente.isActivo()
        );
    }
}
