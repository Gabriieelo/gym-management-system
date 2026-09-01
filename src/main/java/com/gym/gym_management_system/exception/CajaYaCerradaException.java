package com.gym.gym_management_system.exception;

import java.time.LocalDate;

public class CajaYaCerradaException extends RuntimeException {

    public CajaYaCerradaException(LocalDate fecha) {
        super("La caja del " + fecha + " está cerrada");
    }
}
