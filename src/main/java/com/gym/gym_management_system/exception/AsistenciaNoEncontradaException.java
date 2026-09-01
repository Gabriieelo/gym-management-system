package com.gym.gym_management_system.exception;

public class AsistenciaNoEncontradaException extends RuntimeException {

    public AsistenciaNoEncontradaException(Long id) {
        super("No se encontró una asistencia con id " + id);
    }
}
