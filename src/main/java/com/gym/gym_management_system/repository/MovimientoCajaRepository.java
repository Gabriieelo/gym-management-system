package com.gym.gym_management_system.repository;

import com.gym.gym_management_system.entity.MovimientoCaja;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    Optional<MovimientoCaja> findByPagoId(Long pagoId);

    boolean existsByPagoId(Long pagoId);

    List<MovimientoCaja> findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAsc(
            LocalDateTime desde,
            LocalDateTime hasta
    );
}
