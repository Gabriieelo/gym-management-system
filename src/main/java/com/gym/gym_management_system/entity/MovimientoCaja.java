package com.gym.gym_management_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "movimientos_caja")
@Getter
@Setter
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimientoCaja tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrigenMovimientoCaja origen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoMovimientoCaja estado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedioPago medioPago;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", unique = true)
    private Pago pago;
}
