package com.gym.gym_management_system.exception;

public class PasswordActualIncorrectaException extends RuntimeException {

    public PasswordActualIncorrectaException() {
        super("La contraseña actual es incorrecta");
    }
}
