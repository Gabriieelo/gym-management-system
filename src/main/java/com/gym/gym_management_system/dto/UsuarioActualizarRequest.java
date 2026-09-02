package com.gym.gym_management_system.dto;

import com.gym.gym_management_system.entity.RolUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioActualizarRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre completo no puede superar los 120 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Pattern(
                regexp = "[a-zA-Z0-9._-]{3,50}",
                message = "El usuario debe tener entre 3 y 50 caracteres válidos"
        )
        String nombreUsuario,

        @NotNull(message = "El rol es obligatorio")
        RolUsuario rol
) {
}
