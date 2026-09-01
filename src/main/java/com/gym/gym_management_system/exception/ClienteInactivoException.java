package com.gym.gym_management_system.exception;

public class ClienteInactivoException extends RuntimeException {

    public ClienteInactivoException(Long id) {
        super("El cliente con id " + id + " está inactivo");
    }
}
