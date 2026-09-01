package com.gym.gym_management_system.exception;

public class PagoNoEncontradoException extends RuntimeException {

    public PagoNoEncontradoException(Long id) {
        super("No se encontró un pago con id " + id);
    }
}
