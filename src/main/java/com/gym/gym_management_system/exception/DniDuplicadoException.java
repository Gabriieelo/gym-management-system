package com.gym.gym_management_system.exception;

public class DniDuplicadoException extends RuntimeException {

    public DniDuplicadoException(String dni) {
        super("Ya existe un cliente con el DNI " + dni);
    }
}
