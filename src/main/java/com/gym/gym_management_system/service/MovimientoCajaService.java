package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.MovimientoCajaRequest;
import com.gym.gym_management_system.dto.MovimientoCajaResponse;
import com.gym.gym_management_system.dto.ResumenCajaResponse;
import com.gym.gym_management_system.entity.Cliente;
import com.gym.gym_management_system.entity.EstadoMovimientoCaja;
import com.gym.gym_management_system.entity.MovimientoCaja;
import com.gym.gym_management_system.entity.OrigenMovimientoCaja;
import com.gym.gym_management_system.entity.Pago;
import com.gym.gym_management_system.entity.TipoMovimientoCaja;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.exception.MovimientoCajaNoEncontradoException;
import com.gym.gym_management_system.exception.OperacionCajaInvalidaException;
import com.gym.gym_management_system.repository.MovimientoCajaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MovimientoCajaService {

    private final MovimientoCajaRepository movimientoRepository;
    private final Clock reloj;

    public MovimientoCajaService(MovimientoCajaRepository movimientoRepository, Clock reloj) {
        this.movimientoRepository = movimientoRepository;
        this.reloj = reloj;
    }

    public MovimientoCajaResponse registrarManual(MovimientoCajaRequest request) {
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTipo(request.tipo());
        movimiento.setOrigen(OrigenMovimientoCaja.MANUAL);
        movimiento.setEstado(EstadoMovimientoCaja.ACTIVO);
        movimiento.setMonto(request.monto());
        movimiento.setFechaHora(LocalDateTime.now(reloj));
        movimiento.setDescripcion(request.descripcion().trim());
        return convertirAResponse(movimientoRepository.save(movimiento));
    }

    public void registrarDesdePago(Pago pago) {
        if (movimientoRepository.existsByPagoId(pago.getId())) {
            throw new OperacionCajaInvalidaException("El pago ya tiene un movimiento de caja asociado");
        }

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTipo(TipoMovimientoCaja.INGRESO);
        movimiento.setOrigen(OrigenMovimientoCaja.PAGO);
        movimiento.setEstado(EstadoMovimientoCaja.ACTIVO);
        movimiento.setMonto(pago.getMonto());
        movimiento.setFechaHora(pago.getFechaPago());
        movimiento.setDescripcion(describirPago(pago));
        movimiento.setPago(pago);
        movimientoRepository.save(movimiento);
    }

    public MovimientoCajaResponse anularManual(Long id) {
        MovimientoCaja movimiento = buscar(id);
        if (movimiento.getOrigen() == OrigenMovimientoCaja.PAGO) {
            throw new OperacionCajaInvalidaException(
                    "Los movimientos originados por pagos deben anularse desde el pago"
            );
        }
        movimiento.setEstado(EstadoMovimientoCaja.ANULADO);
        return convertirAResponse(movimientoRepository.save(movimiento));
    }

    public void anularPorPago(Long pagoId) {
        movimientoRepository.findByPagoId(pagoId).ifPresent(movimiento -> {
            movimiento.setEstado(EstadoMovimientoCaja.ANULADO);
            movimientoRepository.save(movimiento);
        });
    }

    @Transactional(readOnly = true)
    public List<MovimientoCajaResponse> listarPorFecha(LocalDate fecha) {
        return buscarMovimientos(fecha).stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumenCajaResponse obtenerResumen(LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now(reloj);
        List<MovimientoCaja> movimientos = buscarMovimientos(dia);

        BigDecimal ingresos = sumar(movimientos, TipoMovimientoCaja.INGRESO);
        BigDecimal egresos = sumar(movimientos, TipoMovimientoCaja.EGRESO);
        long activos = movimientos.stream()
                .filter(movimiento -> movimiento.getEstado() == EstadoMovimientoCaja.ACTIVO)
                .count();
        long anulados = movimientos.size() - activos;

        return new ResumenCajaResponse(
                dia,
                ingresos,
                egresos,
                ingresos.subtract(egresos),
                activos,
                anulados
        );
    }

    private MovimientoCaja buscar(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new MovimientoCajaNoEncontradoException(id));
    }

    private List<MovimientoCaja> buscarMovimientos(LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now(reloj);
        return movimientoRepository
                .findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAsc(
                        dia.atStartOfDay(),
                        dia.plusDays(1).atStartOfDay()
                );
    }

    private BigDecimal sumar(List<MovimientoCaja> movimientos, TipoMovimientoCaja tipo) {
        return movimientos.stream()
                .filter(movimiento -> movimiento.getEstado() == EstadoMovimientoCaja.ACTIVO)
                .filter(movimiento -> movimiento.getTipo() == tipo)
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String describirPago(Pago pago) {
        Cliente cliente = pago.getCliente();
        String nombre = cliente.getNombre() + " " + cliente.getApellido();
        if (pago.getTipo() == TipoPago.MENSUAL) {
            return "Cuota mensual " + pago.getPeriodoMes() + "/" + pago.getPeriodoAnio() + " - " + nombre;
        }
        return "Pase diario " + pago.getFechaUso() + " - " + nombre;
    }

    private MovimientoCajaResponse convertirAResponse(MovimientoCaja movimiento) {
        Pago pago = movimiento.getPago();
        Cliente cliente = pago != null ? pago.getCliente() : null;
        return new MovimientoCajaResponse(
                movimiento.getId(),
                movimiento.getTipo(),
                movimiento.getOrigen(),
                movimiento.getEstado(),
                movimiento.getMonto(),
                movimiento.getFechaHora(),
                movimiento.getDescripcion(),
                pago != null ? pago.getId() : null,
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNombre() + " " + cliente.getApellido() : null
        );
    }
}
