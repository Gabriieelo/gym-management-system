package com.gym.gym_management_system.exception;

public class ClienteNoEncontradoException extends RuntimeException {

    public ClienteNoEncontradoException(Long id) {
        super("No se encontró un cliente con id " + id);
    }
}
