package com.gym.gym_management_system.exception;

public class AsistenciaDuplicadaException extends RuntimeException {

    public AsistenciaDuplicadaException(Long clienteId) {
        super("El cliente con id " + clienteId + " ya tiene una asistencia registrada hoy");
    }
}
