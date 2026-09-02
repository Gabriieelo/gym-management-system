package com.gym.gym_management_system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tarifas")
@Getter
@Setter
public class Tarifa {
    @Id
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cuotaEnTermino;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cuotaConRecargo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal paseDiario;
}
