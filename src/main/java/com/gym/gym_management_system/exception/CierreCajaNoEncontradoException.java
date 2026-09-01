package com.gym.gym_management_system.exception;

import java.time.LocalDate;

public class CierreCajaNoEncontradoException extends RuntimeException {

    public CierreCajaNoEncontradoException(LocalDate fecha) {
        super("No se encontró un cierre de caja para la fecha " + fecha);
    }
}
