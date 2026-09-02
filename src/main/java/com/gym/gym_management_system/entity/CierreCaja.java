package com.gym.gym_management_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "cierres_caja",
        uniqueConstraints = @UniqueConstraint(name = "uk_cierre_caja_fecha", columnNames = "fecha")
)
@Getter
@Setter
public class CierreCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoCierreCaja estado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalIngresos;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEgresos;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoGeneral;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal efectivoEsperado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal efectivoContado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal diferenciaEfectivo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal transferenciasEsperadas;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal transferenciasVerificadas;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal diferenciaTransferencias;

    private LocalDateTime fechaHoraCierre;

    private LocalDateTime fechaHoraReapertura;

    @Column(length = 500)
    private String observacion;

    @Column(length = 50)
    private String cerradoPor;

    @Column(length = 50)
    private String reabiertoPor;
}
