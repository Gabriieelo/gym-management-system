package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.TarifaRequest;
import com.gym.gym_management_system.dto.TarifaResponse;
import com.gym.gym_management_system.entity.Tarifa;
import com.gym.gym_management_system.repository.TarifaRepository;
import java.math.BigDecimal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TarifaService {
    private static final Long CONFIGURACION_ID = 1L;
    private final TarifaRepository repository;

    public TarifaService(TarifaRepository repository) {
        this.repository = repository;
    }

    public void inicializar() {
        if (!repository.existsById(CONFIGURACION_ID)) {
            Tarifa tarifa = new Tarifa();
            tarifa.setId(CONFIGURACION_ID);
            tarifa.setCuotaEnTermino(new BigDecimal("38000.00"));
            tarifa.setCuotaConRecargo(new BigDecimal("42000.00"));
            tarifa.setPaseDiario(new BigDecimal("4000.00"));
            repository.save(tarifa);
        }
    }

    @Transactional(readOnly = true)
    public TarifaResponse consultar() {
        return convertir(buscar());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public TarifaResponse actualizar(TarifaRequest request) {
        Tarifa tarifa = buscar();
        tarifa.setCuotaEnTermino(request.cuotaEnTermino());
        tarifa.setCuotaConRecargo(request.cuotaConRecargo());
        tarifa.setPaseDiario(request.paseDiario());
        return convertir(repository.save(tarifa));
    }

    private Tarifa buscar() {
        return repository.findById(CONFIGURACION_ID)
                .orElseThrow(() -> new IllegalStateException("No se inicializó la configuración de tarifas"));
    }

    private TarifaResponse convertir(Tarifa tarifa) {
        return new TarifaResponse(
                tarifa.getCuotaEnTermino(), tarifa.getCuotaConRecargo(), tarifa.getPaseDiario());
    }
}
