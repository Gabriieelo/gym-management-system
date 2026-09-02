package com.gym.gym_management_system.repository;

import com.gym.gym_management_system.entity.Asistencia;
import com.gym.gym_management_system.entity.EstadoAsistencia;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    boolean existsByClienteIdAndFechaAndEstado(
            Long clienteId,
            LocalDate fecha,
            EstadoAsistencia estado
    );

    List<Asistencia> findByFechaOrderByFechaHoraAsc(LocalDate fecha);

    List<Asistencia> findByClienteIdOrderByFechaHoraDesc(Long clienteId);

    long countByFechaAndEstado(LocalDate fecha, EstadoAsistencia estado);
}
