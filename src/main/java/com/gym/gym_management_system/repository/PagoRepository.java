package com.gym.gym_management_system.repository;

import com.gym.gym_management_system.entity.EstadoPago;
import com.gym.gym_management_system.entity.Pago;
import com.gym.gym_management_system.entity.TipoPago;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByClienteIdAndTipoAndPeriodoMesAndPeriodoAnioAndEstado(
            Long clienteId,
            TipoPago tipo,
            Integer periodoMes,
            Integer periodoAnio,
            EstadoPago estado
    );

    boolean existsByClienteIdAndTipoAndFechaUsoAndEstado(
            Long clienteId,
            TipoPago tipo,
            LocalDate fechaUso,
            EstadoPago estado
    );

    List<Pago> findByFechaPagoGreaterThanEqualAndFechaPagoLessThanOrderByFechaPagoAsc(
            LocalDateTime desde,
            LocalDateTime hasta
    );

    List<Pago> findByClienteIdOrderByFechaPagoDesc(Long clienteId);

    List<Pago> findByFechaUsoAndTipoAndEstadoOrderByFechaPagoAsc(
            LocalDate fechaUso,
            TipoPago tipo,
            EstadoPago estado
    );

    Optional<Pago> findFirstByClienteIdAndTipoAndEstadoOrderByPeriodoAnioDescPeriodoMesDesc(
            Long clienteId,
            TipoPago tipo,
            EstadoPago estado
    );

    long countByTipoAndEstadoAndFechaPagoGreaterThanEqualAndFechaPagoLessThan(
            TipoPago tipo,
            EstadoPago estado,
            LocalDateTime desde,
            LocalDateTime hasta
    );
}
