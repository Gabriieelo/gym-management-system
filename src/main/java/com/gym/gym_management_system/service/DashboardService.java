package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.DashboardResponse;
import com.gym.gym_management_system.dto.EstadoCuotaResponse;
import com.gym.gym_management_system.dto.ResumenCajaResponse;
import com.gym.gym_management_system.entity.EstadoAsistencia;
import com.gym.gym_management_system.entity.EstadoCierreCaja;
import com.gym.gym_management_system.entity.EstadoCuota;
import com.gym.gym_management_system.entity.EstadoPago;
import com.gym.gym_management_system.entity.TipoPago;
import com.gym.gym_management_system.repository.AsistenciaRepository;
import com.gym.gym_management_system.repository.CierreCajaRepository;
import com.gym.gym_management_system.repository.ClienteRepository;
import com.gym.gym_management_system.repository.PagoRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ClienteRepository clienteRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final PagoRepository pagoRepository;
    private final CierreCajaRepository cierreCajaRepository;
    private final EstadoCuotaService estadoCuotaService;
    private final MovimientoCajaService movimientoCajaService;
    private final Clock reloj;

    public DashboardService(
            ClienteRepository clienteRepository,
            AsistenciaRepository asistenciaRepository,
            PagoRepository pagoRepository,
            CierreCajaRepository cierreCajaRepository,
            EstadoCuotaService estadoCuotaService,
            MovimientoCajaService movimientoCajaService,
            Clock reloj) {
        this.clienteRepository = clienteRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.pagoRepository = pagoRepository;
        this.cierreCajaRepository = cierreCajaRepository;
        this.estadoCuotaService = estadoCuotaService;
        this.movimientoCajaService = movimientoCajaService;
        this.reloj = reloj;
    }

    public DashboardResponse obtenerResumen() {
        LocalDate hoy = LocalDate.now(reloj);
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.plusDays(1).atStartOfDay();
        LocalDateTime inicioMes = YearMonth.from(hoy).atDay(1).atStartOfDay();
        LocalDateTime finMes = YearMonth.from(hoy).plusMonths(1).atDay(1).atStartOfDay();
        List<EstadoCuotaResponse> cuotas = estadoCuotaService.listarActivos();
        ResumenCajaResponse cajaDia = movimientoCajaService.obtenerResumen(hoy);
        BigDecimal ingresosMes = movimientoCajaService.obtenerIngresosEntre(inicioMes, finMes);
        EstadoCierreCaja estadoCaja = cierreCajaRepository.findByFecha(hoy)
                .map(cierre -> cierre.getEstado())
                .orElse(EstadoCierreCaja.ABIERTO);

        return new DashboardResponse(
                hoy,
                clienteRepository.countByActivo(true),
                clienteRepository.countByActivo(false),
                contar(cuotas, EstadoCuota.AL_DIA),
                contar(cuotas, EstadoCuota.PENDIENTE),
                contar(cuotas, EstadoCuota.VENCIDA),
                asistenciaRepository.countByFechaAndEstado(hoy, EstadoAsistencia.REGISTRADA),
                pagoRepository.countByTipoAndEstadoAndFechaPagoGreaterThanEqualAndFechaPagoLessThan(
                        TipoPago.MENSUAL, EstadoPago.CONFIRMADO, inicioMes, finMes),
                pagoRepository.countByTipoAndEstadoAndFechaPagoGreaterThanEqualAndFechaPagoLessThan(
                        TipoPago.PASE_DIARIO, EstadoPago.CONFIRMADO, inicioDia, finDia),
                cajaDia.totalIngresos(),
                ingresosMes,
                cajaDia.ingresosEfectivo(),
                cajaDia.ingresosTransferencia(),
                estadoCaja
        );
    }

    private long contar(List<EstadoCuotaResponse> cuotas, EstadoCuota estado) {
        return cuotas.stream().filter(cuota -> cuota.estado() == estado).count();
    }
}
