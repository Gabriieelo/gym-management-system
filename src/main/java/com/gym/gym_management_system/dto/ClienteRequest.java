package com.gym.gym_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ClienteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String apellido,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{7,10}", message = "El DNI debe contener entre 7 y 10 dígitos")
        String dni,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
        String telefono,

        @Size(max = 100, message = "El contacto de emergencia no puede superar los 100 caracteres")
        String contactoEmergencia,

        @Size(max = 500, message = "La URL de la foto no puede superar los 500 caracteres")
        String fotoUrl,

        LocalDate fechaIngreso
) {
}
