package com.gym.gym_management_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 50)
    private String nombreUsuario;

    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RolUsuario rol;

    @Column(nullable = false)
    private boolean activo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    private void antesDeCrear() {
        LocalDateTime ahora = LocalDateTime.now();
        fechaCreacion = ahora;
        fechaActualizacion = ahora;
        activo = true;
    }

    @PreUpdate
    private void antesDeActualizar() {
        fechaActualizacion = LocalDateTime.now();
    }
}
