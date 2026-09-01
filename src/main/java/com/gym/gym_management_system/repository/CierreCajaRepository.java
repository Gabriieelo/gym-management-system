package com.gym.gym_management_system.repository;

import com.gym.gym_management_system.entity.CierreCaja;
import com.gym.gym_management_system.entity.EstadoCierreCaja;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {

    Optional<CierreCaja> findByFecha(LocalDate fecha);

    boolean existsByFechaAndEstado(LocalDate fecha, EstadoCierreCaja estado);

    List<CierreCaja> findAllByOrderByFechaDesc();
}
