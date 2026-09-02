package com.gym.gym_management_system.dto;

import com.gym.gym_management_system.entity.EstadoAsistencia;
import com.gym.gym_management_system.entity.ModalidadAcceso;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AsistenciaResponse(
        Long id,
        Long clienteId,
        String cliente,
        LocalDate fecha,
        LocalDateTime fechaHora,
        ModalidadAcceso modalidad,
        EstadoAsistencia estado,
        String registradoPor,
        String anuladoPor
) {
}
