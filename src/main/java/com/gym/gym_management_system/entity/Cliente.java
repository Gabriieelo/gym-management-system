package com.gym.gym_management_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "clientes")
@Getter
@Setter
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 10)
    private String dni;

    @Column(length = 30)
    private String telefono;

    @Column(length = 100)
    private String contactoEmergencia;

    @Column(length = 500)
    private String fotoUrl;

    @Column(nullable = false)
    private LocalDate fechaIngreso;

    @Column(nullable = false)
    private boolean activo;

    @PrePersist
    private void establecerValoresIniciales() {
        if (fechaIngreso == null) {
            fechaIngreso = LocalDate.now();
        }
        activo = true;
    }
}
