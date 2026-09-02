package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.ClienteBusquedaResponse;
import com.gym.gym_management_system.dto.EstadoCuotaResponse;
import com.gym.gym_management_system.dto.PaginaResponse;
import com.gym.gym_management_system.entity.Cliente;
import com.gym.gym_management_system.entity.EstadoCuota;
import com.gym.gym_management_system.entity.EstadoPago;
import com.gym.gym_management_system.entity.Pago;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.repository.ClienteRepository;
import jakarta.persistence.criteria.Subquery;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClienteBusquedaService {

    private final ClienteRepository clienteRepository;
    private final EstadoCuotaService estadoCuotaService;
    private final CalculadorTarifa calculadorTarifa;
    private final Clock reloj;

    public ClienteBusquedaService(
            ClienteRepository clienteRepository,
            EstadoCuotaService estadoCuotaService,
            CalculadorTarifa calculadorTarifa,
            Clock reloj) {
        this.clienteRepository = clienteRepository;
        this.estadoCuotaService = estadoCuotaService;
        this.calculadorTarifa = calculadorTarifa;
        this.reloj = reloj;
    }

    public PaginaResponse<ClienteBusquedaResponse> buscar(
            String texto, Boolean activo, EstadoCuota estadoCuota, int pagina, int tamanio) {
        PageRequest pageable = PageRequest.of(
                pagina,
                tamanio,
                Sort.by("apellido").ascending().and(Sort.by("nombre").ascending()));

        Specification<Cliente> filtros = Specification
                .where(filtrarTexto(texto))
                .and(filtrarActivo(activo))
                .and(filtrarEstadoCuota(estadoCuota));

        Page<ClienteBusquedaResponse> resultado = clienteRepository.findAll(filtros, pageable)
                .map(this::convertir);
        return PaginaResponse.desde(resultado);
    }

    private Specification<Cliente> filtrarTexto(String texto) {
        return (root, query, cb) -> {
            if (texto == null || texto.isBlank()) {
                return cb.conjunction();
            }
            String patron = "%" + texto.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nombre")), patron),
                    cb.like(cb.lower(root.get("apellido")), patron),
                    cb.like(cb.lower(root.get("dni")), patron),
                    cb.like(
                            cb.lower(cb.concat(cb.concat(root.get("nombre"), " "), root.get("apellido"))),
                            patron));
        };
    }

    private Specification<Cliente> filtrarActivo(Boolean activo) {
        return (root, query, cb) -> activo == null
                ? cb.conjunction()
                : cb.equal(root.get("activo"), activo);
    }

    private Specification<Cliente> filtrarEstadoCuota(EstadoCuota estadoSolicitado) {
        return (root, query, cb) -> {
            if (estadoSolicitado == null) {
                return cb.conjunction();
            }

            LocalDate hoy = LocalDate.now(reloj);
            EstadoCuota estadoImpagoActual = hoy.isAfter(calculadorTarifa.calcularLimiteSinRecargo(hoy))
                    ? EstadoCuota.VENCIDA
                    : EstadoCuota.PENDIENTE;
            if (estadoSolicitado != EstadoCuota.AL_DIA && estadoSolicitado != estadoImpagoActual) {
                return cb.disjunction();
            }

            YearMonth periodo = YearMonth.from(hoy);
            Subquery<Long> pagoActual = query.subquery(Long.class);
            var pago = pagoActual.from(Pago.class);
            pagoActual.select(pago.get("id")).where(
                    cb.equal(pago.get("cliente").get("id"), root.get("id")),
                    cb.equal(pago.get("tipo"), TipoPago.MENSUAL),
                    cb.equal(pago.get("estado"), EstadoPago.CONFIRMADO),
                    cb.equal(pago.get("periodoMes"), periodo.getMonthValue()),
                    cb.equal(pago.get("periodoAnio"), periodo.getYear()));

            return estadoSolicitado == EstadoCuota.AL_DIA
                    ? cb.exists(pagoActual)
                    : cb.not(cb.exists(pagoActual));
        };
    }

    private ClienteBusquedaResponse convertir(Cliente cliente) {
        EstadoCuotaResponse cuota = estadoCuotaService.calcularEstado(cliente);
        return new ClienteBusquedaResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDni(),
                cliente.getTelefono(),
                cliente.getFechaIngreso(),
                cliente.isActivo(),
                cuota.estado(),
                cuota.ultimoPeriodoPagado());
    }
}
