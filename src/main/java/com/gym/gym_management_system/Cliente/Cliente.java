package com.gym.gym_management_system.Cliente;

import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Cliente {
    
    private long Id;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String contactoEmergencia;
    private String fotoUrl;
}
