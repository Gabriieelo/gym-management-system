package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.CierreCajaRequest;
import com.gym.gym_management_system.dto.CierreCajaResponse;
import com.gym.gym_management_system.dto.ResumenCajaResponse;
import com.gym.gym_management_system.entity.CierreCaja;
import com.gym.gym_management_system.entity.EstadoCierreCaja;
import com.gym.gym_management_system.exception.CajaYaCerradaException;
import com.gym.gym_management_system.exception.CierreCajaNoEncontradoException;
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
public class CierreCajaService {

    private final CierreCajaRepository cierreRepository;
    private final MovimientoCajaService movimientoCajaService;
    private final Clock reloj;

    public CierreCajaService(
            CierreCajaRepository cierreRepository,
            MovimientoCajaService movimientoCajaService,
            Clock reloj) {
        this.cierreRepository = cierreRepository;
        this.movimientoCajaService = movimientoCajaService;
        this.reloj = reloj;
    }

    public CierreCajaResponse cerrar(CierreCajaRequest request) {
        LocalDate fecha = request.fecha() != null ? request.fecha() : LocalDate.now(reloj);
        CierreCaja cierre = cierreRepository.findByFecha(fecha).orElseGet(CierreCaja::new);
        if (cierre.getEstado() == EstadoCierreCaja.CERRADO) {
            throw new CajaYaCerradaException(fecha);
        }

        ResumenCajaResponse resumen = movimientoCajaService.obtenerResumen(fecha);
        BigDecimal efectivoEsperado = resumen.saldoEfectivo();
        BigDecimal transferenciasEsperadas = resumen.ingresosTransferencia();

        cierre.setFecha(fecha);
        cierre.setEstado(EstadoCierreCaja.CERRADO);
        cierre.setTotalIngresos(resumen.totalIngresos());
        cierre.setTotalEgresos(resumen.totalEgresos());
        cierre.setSaldoGeneral(resumen.saldo());
        cierre.setEfectivoEsperado(efectivoEsperado);
        cierre.setEfectivoContado(request.efectivoContado());
        cierre.setDiferenciaEfectivo(request.efectivoContado().subtract(efectivoEsperado));
        cierre.setTransferenciasEsperadas(transferenciasEsperadas);
        cierre.setTransferenciasVerificadas(request.transferenciasVerificadas());
        cierre.setDiferenciaTransferencias(
                request.transferenciasVerificadas().subtract(transferenciasEsperadas));
        cierre.setFechaHoraCierre(LocalDateTime.now(reloj));
        cierre.setObservacion(limpiarTexto(request.observacion()));
        return convertirAResponse(cierreRepository.save(cierre));
    }

    public CierreCajaResponse reabrir(LocalDate fecha) {
        CierreCaja cierre = cierreRepository.findByFecha(fecha)
                .orElseThrow(() -> new CierreCajaNoEncontradoException(fecha));
        cierre.setEstado(EstadoCierreCaja.ABIERTO);
        cierre.setFechaHoraReapertura(LocalDateTime.now(reloj));
        return convertirAResponse(cierreRepository.save(cierre));
    }

    @Transactional(readOnly = true)
    public CierreCajaResponse buscarPorFecha(LocalDate fecha) {
        return cierreRepository.findByFecha(fecha)
                .map(this::convertirAResponse)
                .orElseThrow(() -> new CierreCajaNoEncontradoException(fecha));
    }

    @Transactional(readOnly = true)
    public List<CierreCajaResponse> listar() {
        return cierreRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    private String limpiarTexto(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private CierreCajaResponse convertirAResponse(CierreCaja cierre) {
        return new CierreCajaResponse(
                cierre.getId(),
                cierre.getFecha(),
                cierre.getEstado(),
                cierre.getTotalIngresos(),
                cierre.getTotalEgresos(),
                cierre.getSaldoGeneral(),
                cierre.getEfectivoEsperado(),
                cierre.getEfectivoContado(),
                cierre.getDiferenciaEfectivo(),
                cierre.getTransferenciasEsperadas(),
                cierre.getTransferenciasVerificadas(),
                cierre.getDiferenciaTransferencias(),
                esCero(cierre.getDiferenciaEfectivo()),
                esCero(cierre.getDiferenciaTransferencias()),
                cierre.getFechaHoraCierre(),
                cierre.getFechaHoraReapertura(),
                cierre.getObservacion()
        );
    }

    private boolean esCero(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) == 0;
    }
}
