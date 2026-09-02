package com.gym.gym_management_system.exception;

public class UsuarioDuplicadoException extends RuntimeException {

    public UsuarioDuplicadoException(String nombreUsuario) {
        super("Ya existe un usuario con el nombre " + nombreUsuario);
    }
}
