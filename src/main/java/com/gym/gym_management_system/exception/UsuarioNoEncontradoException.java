package com.gym.gym_management_system.exception;

public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(Object identificador) {
        super("No se encontró el usuario " + identificador);
    }
}
