package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.EstadoCuotaResponse;
import com.gym.gym_management_system.entity.Cliente;
import com.gym.gym_management_system.entity.EstadoCuota;
import com.gym.gym_management_system.entity.EstadoPago;
import com.gym.gym_management_system.entity.Pago;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.exception.ClienteNoEncontradoException;
import com.gym.gym_management_system.repository.ClienteRepository;
import com.gym.gym_management_system.repository.PagoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EstadoCuotaService {

    private final ClienteRepository clienteRepository;
    private final PagoRepository pagoRepository;
    private final CalculadorTarifa calculadorTarifa;
    private final Clock reloj;

    public EstadoCuotaService(
            ClienteRepository clienteRepository,
            PagoRepository pagoRepository,
            CalculadorTarifa calculadorTarifa,
            Clock reloj) {
        this.clienteRepository = clienteRepository;
        this.pagoRepository = pagoRepository;
        this.calculadorTarifa = calculadorTarifa;
        this.reloj = reloj;
    }

    public EstadoCuotaResponse consultar(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
        return calcularEstado(cliente);
    }

    public List<EstadoCuotaResponse> listarVencidas() {
        return listarActivos().stream()
                .filter(response -> response.estado() == EstadoCuota.VENCIDA)
                .toList();
    }

    public List<EstadoCuotaResponse> listarActivos() {
        return clienteRepository.findByActivo(true).stream()
                .map(this::calcularEstado)
                .toList();
    }

    EstadoCuotaResponse calcularEstado(Cliente cliente) {
        LocalDate hoy = LocalDate.now(reloj);
        YearMonth periodoActual = YearMonth.from(hoy);
        LocalDate fechaLimite = calculadorTarifa.calcularLimiteSinRecargo(hoy);
        boolean pagada = pagoRepository.existsByClienteIdAndTipoAndPeriodoMesAndPeriodoAnioAndEstado(
                cliente.getId(), TipoPago.MENSUAL, periodoActual.getMonthValue(),
                periodoActual.getYear(), EstadoPago.CONFIRMADO);

        EstadoCuota estado = pagada
                ? EstadoCuota.AL_DIA
                : hoy.isAfter(fechaLimite) ? EstadoCuota.VENCIDA : EstadoCuota.PENDIENTE;

        Optional<Pago> ultimoPago = pagoRepository
                .findFirstByClienteIdAndTipoAndEstadoOrderByPeriodoAnioDescPeriodoMesDesc(
                        cliente.getId(), TipoPago.MENSUAL, EstadoPago.CONFIRMADO);

        return new EstadoCuotaResponse(
                cliente.getId(),
                cliente.getNombre() + " " + cliente.getApellido(),
                periodoActual,
                pagada,
                estado,
                fechaLimite,
                ultimoPago.map(Pago::getFechaPago).map(fecha -> fecha.toLocalDate()).orElse(null),
                ultimoPago.map(pago -> YearMonth.of(pago.getPeriodoAnio(), pago.getPeriodoMes())).orElse(null)
        );
    }
}
