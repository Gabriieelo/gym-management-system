package com.gym.gym_management_system.exception;

public class MovimientoCajaNoEncontradoException extends RuntimeException {

    public MovimientoCajaNoEncontradoException(Long id) {
        super("No se encontró un movimiento de caja con id " + id);
    }
}
