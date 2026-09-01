package com.gym.gym_management_system.exception;

public class AccesoNoHabilitadoException extends RuntimeException {

    public AccesoNoHabilitadoException(Long clienteId) {
        super("El cliente con id " + clienteId + " no tiene cuota mensual ni pase diario vigente");
    }
}
