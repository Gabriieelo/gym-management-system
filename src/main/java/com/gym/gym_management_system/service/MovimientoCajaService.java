package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.MovimientoCajaRequest;
import com.gym.gym_management_system.dto.MovimientoCajaResponse;
import com.gym.gym_management_system.dto.ResumenCajaResponse;
import com.gym.gym_management_system.entity.Cliente;
import com.gym.gym_management_system.entity.EstadoMovimientoCaja;
import com.gym.gym_management_system.entity.EstadoCierreCaja;
import com.gym.gym_management_system.entity.MovimientoCaja;
import com.gym.gym_management_system.entity.MedioPago;
import com.gym.gym_management_system.entity.OrigenMovimientoCaja;
import com.gym.gym_management_system.entity.Pago;
import com.gym.gym_management_system.entity.TipoMovimientoCaja;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.exception.MovimientoCajaNoEncontradoException;
import com.gym.gym_management_system.exception.CajaYaCerradaException;
import com.gym.gym_management_system.exception.OperacionCajaInvalidaException;
import com.gym.gym_management_system.repository.MovimientoCajaRepository;
import com.gym.gym_management_system.repository.CierreCajaRepository;
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
    private final CierreCajaRepository cierreRepository;
    private final Clock reloj;

    public MovimientoCajaService(
            MovimientoCajaRepository movimientoRepository,
            CierreCajaRepository cierreRepository,
            Clock reloj) {
        this.movimientoRepository = movimientoRepository;
        this.cierreRepository = cierreRepository;
        this.reloj = reloj;
    }

    public MovimientoCajaResponse registrarManual(MovimientoCajaRequest request) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        validarCajaAbierta(ahora.toLocalDate());
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTipo(request.tipo());
        movimiento.setOrigen(OrigenMovimientoCaja.MANUAL);
        movimiento.setEstado(EstadoMovimientoCaja.ACTIVO);
        movimiento.setMonto(request.monto());
        movimiento.setMedioPago(request.medioPago());
        movimiento.setFechaHora(ahora);
        movimiento.setDescripcion(request.descripcion().trim());
        return convertirAResponse(movimientoRepository.save(movimiento));
    }

    public void registrarDesdePago(Pago pago) {
        validarCajaAbierta(pago.getFechaPago().toLocalDate());
        if (movimientoRepository.existsByPagoId(pago.getId())) {
            throw new OperacionCajaInvalidaException("El pago ya tiene un movimiento de caja asociado");
        }

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTipo(TipoMovimientoCaja.INGRESO);
        movimiento.setOrigen(OrigenMovimientoCaja.PAGO);
        movimiento.setEstado(EstadoMovimientoCaja.ACTIVO);
        movimiento.setMonto(pago.getMonto());
        movimiento.setMedioPago(pago.getMedioPago());
        movimiento.setFechaHora(pago.getFechaPago());
        movimiento.setDescripcion(describirPago(pago));
        movimiento.setPago(pago);
        movimientoRepository.save(movimiento);
    }

    public MovimientoCajaResponse anularManual(Long id) {
        MovimientoCaja movimiento = buscar(id);
        validarCajaAbierta(movimiento.getFechaHora().toLocalDate());
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
            validarCajaAbierta(movimiento.getFechaHora().toLocalDate());
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
        BigDecimal ingresosEfectivo = sumar(
                movimientos, TipoMovimientoCaja.INGRESO, MedioPago.EFECTIVO);
        BigDecimal ingresosTransferencia = sumar(
                movimientos, TipoMovimientoCaja.INGRESO, MedioPago.TRANSFERENCIA);
        BigDecimal egresosEfectivo = sumar(
                movimientos, TipoMovimientoCaja.EGRESO, MedioPago.EFECTIVO);
        BigDecimal egresosTransferencia = sumar(
                movimientos, TipoMovimientoCaja.EGRESO, MedioPago.TRANSFERENCIA);
        long activos = movimientos.stream()
                .filter(movimiento -> movimiento.getEstado() == EstadoMovimientoCaja.ACTIVO)
                .count();
        long anulados = movimientos.size() - activos;

        return new ResumenCajaResponse(
                dia,
                ingresos,
                ingresosEfectivo,
                ingresosTransferencia,
                egresos,
                egresosEfectivo,
                egresosTransferencia,
                ingresos.subtract(egresos),
                ingresosEfectivo.subtract(egresosEfectivo),
                ingresosTransferencia.subtract(egresosTransferencia),
                activos,
                anulados
        );
    }

    private MovimientoCaja buscar(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new MovimientoCajaNoEncontradoException(id));
    }

    private void validarCajaAbierta(LocalDate fecha) {
        if (cierreRepository.existsByFechaAndEstado(fecha, EstadoCierreCaja.CERRADO)) {
            throw new CajaYaCerradaException(fecha);
        }
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

    private BigDecimal sumar(
            List<MovimientoCaja> movimientos,
            TipoMovimientoCaja tipo,
            MedioPago medioPago) {
        return movimientos.stream()
                .filter(movimiento -> movimiento.getEstado() == EstadoMovimientoCaja.ACTIVO)
                .filter(movimiento -> movimiento.getTipo() == tipo)
                .filter(movimiento -> movimiento.getMedioPago() == medioPago)
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
                movimiento.getMedioPago(),
                movimiento.getFechaHora(),
                movimiento.getDescripcion(),
                pago != null ? pago.getId() : null,
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNombre() + " " + cliente.getApellido() : null
        );
    }
}
