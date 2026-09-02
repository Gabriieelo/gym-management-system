package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.EstadoCuotaResponse;
import com.gym.gym_management_system.dto.PagoRequest;
import com.gym.gym_management_system.dto.PagoResponse;
import com.gym.gym_management_system.entity.Cliente;
import com.gym.gym_management_system.entity.EstadoPago;
import com.gym.gym_management_system.entity.Pago;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.exception.ClienteInactivoException;
import com.gym.gym_management_system.exception.ClienteNoEncontradoException;
import com.gym.gym_management_system.exception.PagoDuplicadoException;
import com.gym.gym_management_system.exception.PagoNoEncontradoException;
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
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ClienteRepository clienteRepository;
    private final CalculadorTarifa calculadorTarifa;
    private final MovimientoCajaService movimientoCajaService;
    private final UsuarioActualService usuarioActualService;
    private final EstadoCuotaService estadoCuotaService;
    private final Clock reloj;

    public PagoService(
            PagoRepository pagoRepository,
            ClienteRepository clienteRepository,
            CalculadorTarifa calculadorTarifa,
            MovimientoCajaService movimientoCajaService,
            UsuarioActualService usuarioActualService,
            EstadoCuotaService estadoCuotaService,
            Clock reloj) {
        this.pagoRepository = pagoRepository;
        this.clienteRepository = clienteRepository;
        this.calculadorTarifa = calculadorTarifa;
        this.movimientoCajaService = movimientoCajaService;
        this.usuarioActualService = usuarioActualService;
        this.estadoCuotaService = estadoCuotaService;
        this.reloj = reloj;
    }

    public PagoResponse registrar(PagoRequest request) {
        Cliente cliente = buscarCliente(request.clienteId());
        if (!cliente.isActivo()) {
            throw new ClienteInactivoException(cliente.getId());
        }

        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDate fechaActual = ahora.toLocalDate();
        validarDuplicado(cliente.getId(), request.tipo(), fechaActual);

        Pago pago = new Pago();
        pago.setCliente(cliente);
        pago.setTipo(request.tipo());
        pago.setEstado(EstadoPago.CONFIRMADO);
        pago.setMonto(calculadorTarifa.calcular(request.tipo(), fechaActual));
        pago.setMedioPago(request.medioPago());
        pago.setFechaPago(ahora);
        pago.setObservacion(limpiarTexto(request.observacion()));
        pago.setRegistradoPor(usuarioActualService.obtenerNombreUsuario());

        if (request.tipo() == TipoPago.MENSUAL) {
            pago.setPeriodoMes(fechaActual.getMonthValue());
            pago.setPeriodoAnio(fechaActual.getYear());
        } else {
            pago.setFechaUso(fechaActual);
        }

        Pago pagoGuardado = pagoRepository.save(pago);
        movimientoCajaService.registrarDesdePago(pagoGuardado);
        return convertirAResponse(pagoGuardado);
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorFecha(LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now(reloj);
        LocalDateTime desde = dia.atStartOfDay();
        LocalDateTime hasta = dia.plusDays(1).atStartOfDay();

        return pagoRepository
                .findByFechaPagoGreaterThanEqualAndFechaPagoLessThanOrderByFechaPagoAsc(desde, hasta)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorCliente(Long clienteId) {
        buscarCliente(clienteId);
        return pagoRepository.findByClienteIdOrderByFechaPagoDesc(clienteId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EstadoCuotaResponse consultarCuotaActual(Long clienteId) {
        return estadoCuotaService.consultar(clienteId);
    }

    public PagoResponse anular(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new PagoNoEncontradoException(id));
        pago.setEstado(EstadoPago.ANULADO);
        pago.setAnuladoPor(usuarioActualService.obtenerNombreUsuario());
        Pago pagoAnulado = pagoRepository.save(pago);
        movimientoCajaService.anularPorPago(pagoAnulado.getId());
        return convertirAResponse(pagoAnulado);
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    private void validarDuplicado(Long clienteId, TipoPago tipo, LocalDate fecha) {
        boolean existe;
        if (tipo == TipoPago.MENSUAL) {
            existe = pagoRepository.existsByClienteIdAndTipoAndPeriodoMesAndPeriodoAnioAndEstado(
                    clienteId,
                    tipo,
                    fecha.getMonthValue(),
                    fecha.getYear(),
                    EstadoPago.CONFIRMADO
            );
        } else {
            existe = pagoRepository.existsByClienteIdAndTipoAndFechaUsoAndEstado(
                    clienteId,
                    tipo,
                    fecha,
                    EstadoPago.CONFIRMADO
            );
        }

        if (existe) {
            throw new PagoDuplicadoException(
                    tipo == TipoPago.MENSUAL
                            ? "El cliente ya pagó la cuota del mes actual"
                            : "El cliente ya pagó un pase para el día de hoy"
            );
        }
    }

    private String limpiarTexto(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private PagoResponse convertirAResponse(Pago pago) {
        Cliente cliente = pago.getCliente();
        return new PagoResponse(
                pago.getId(),
                cliente.getId(),
                cliente.getNombre() + " " + cliente.getApellido(),
                pago.getTipo(),
                pago.getEstado(),
                pago.getMonto(),
                pago.getMedioPago(),
                pago.getFechaPago(),
                pago.getPeriodoMes(),
                pago.getPeriodoAnio(),
                pago.getFechaUso(),
                pago.getObservacion(),
                pago.getRegistradoPor(),
                pago.getAnuladoPor()
        );
    }
}
