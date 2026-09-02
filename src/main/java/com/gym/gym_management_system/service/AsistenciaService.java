package com.gym.gym_management_system.service;

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
import com.gym.gym_management_system.exception.AsistenciaNoEncontradaException;
import com.gym.gym_management_system.exception.ClienteInactivoException;
import com.gym.gym_management_system.exception.ClienteNoEncontradoException;
import com.gym.gym_management_system.repository.AsistenciaRepository;
import com.gym.gym_management_system.repository.ClienteRepository;
import com.gym.gym_management_system.repository.PagoRepository;
import com.gym.gym_management_system.security.UsuarioActualService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final ClienteRepository clienteRepository;
    private final PagoRepository pagoRepository;
    private final Clock reloj;
    private final UsuarioActualService usuarioActualService;

    public AsistenciaService(
            AsistenciaRepository asistenciaRepository,
            ClienteRepository clienteRepository,
            PagoRepository pagoRepository,
            UsuarioActualService usuarioActualService,
            Clock reloj) {
        this.asistenciaRepository = asistenciaRepository;
        this.clienteRepository = clienteRepository;
        this.pagoRepository = pagoRepository;
        this.usuarioActualService = usuarioActualService;
        this.reloj = reloj;
    }

    public AsistenciaResponse registrar(AsistenciaRequest request) {
        Cliente cliente = buscarCliente(request.clienteId());
        if (!cliente.isActivo()) {
            throw new ClienteInactivoException(cliente.getId());
        }

        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDate fecha = ahora.toLocalDate();
        if (asistenciaRepository.existsByClienteIdAndFechaAndEstado(
                cliente.getId(), fecha, EstadoAsistencia.REGISTRADA)) {
            throw new AsistenciaDuplicadaException(cliente.getId());
        }

        ModalidadAcceso modalidad = determinarModalidad(cliente.getId(), fecha);

        Asistencia asistencia = new Asistencia();
        asistencia.setCliente(cliente);
        asistencia.setFecha(fecha);
        asistencia.setFechaHora(ahora);
        asistencia.setModalidad(modalidad);
        asistencia.setEstado(EstadoAsistencia.REGISTRADA);
        asistencia.setRegistradoPor(usuarioActualService.obtenerNombreUsuario());
        return convertirAResponse(asistenciaRepository.save(asistencia));
    }

    @Transactional(readOnly = true)
    public List<AsistenciaResponse> listarPorFecha(LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now(reloj);
        return asistenciaRepository.findByFechaOrderByFechaHoraAsc(dia)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsistenciaResponse> listarPorCliente(Long clienteId) {
        buscarCliente(clienteId);
        return asistenciaRepository.findByClienteIdOrderByFechaHoraDesc(clienteId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public AsistenciaResponse anular(Long id) {
        Asistencia asistencia = asistenciaRepository.findById(id)
                .orElseThrow(() -> new AsistenciaNoEncontradaException(id));
        asistencia.setEstado(EstadoAsistencia.ANULADA);
        asistencia.setAnuladoPor(usuarioActualService.obtenerNombreUsuario());
        return convertirAResponse(asistenciaRepository.save(asistencia));
    }

    private ModalidadAcceso determinarModalidad(Long clienteId, LocalDate fecha) {
        boolean tieneCuota = pagoRepository
                .existsByClienteIdAndTipoAndPeriodoMesAndPeriodoAnioAndEstado(
                        clienteId,
                        TipoPago.MENSUAL,
                        fecha.getMonthValue(),
                        fecha.getYear(),
                        EstadoPago.CONFIRMADO
                );
        if (tieneCuota) {
            return ModalidadAcceso.MENSUAL;
        }

        boolean tienePase = pagoRepository.existsByClienteIdAndTipoAndFechaUsoAndEstado(
                clienteId,
                TipoPago.PASE_DIARIO,
                fecha,
                EstadoPago.CONFIRMADO
        );
        if (tienePase) {
            return ModalidadAcceso.PASE_DIARIO;
        }

        throw new AccesoNoHabilitadoException(clienteId);
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    private AsistenciaResponse convertirAResponse(Asistencia asistencia) {
        Cliente cliente = asistencia.getCliente();
        return new AsistenciaResponse(
                asistencia.getId(),
                cliente.getId(),
                cliente.getNombre() + " " + cliente.getApellido(),
                asistencia.getFecha(),
                asistencia.getFechaHora(),
                asistencia.getModalidad(),
                asistencia.getEstado(),
                asistencia.getRegistradoPor(),
                asistencia.getAnuladoPor()
        );
    }
}
