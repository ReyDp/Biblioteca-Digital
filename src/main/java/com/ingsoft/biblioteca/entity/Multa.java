
package com.ingsoft.biblioteca.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "multas")
public class Multa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestamo_id", nullable = false, unique = true)
    private Prestamo prestamo;

    @Column(name = "tarifa_diaria", nullable = false, precision = 12, scale = 2)
    private BigDecimal tarifaDiaria = new BigDecimal("1000.00");

    @Column(name = "dias_retraso_finales", nullable = false)
    private int diasRetrasoFinales;

    @Column(name = "importe_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeTotal = BigDecimal.ZERO;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDate fechaCalculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMulta estado = EstadoMulta.PENDIENTE;

    public Multa() {
    }

    @PrePersist
    protected void alCrear() {
        if (fechaCalculo == null) {
            fechaCalculo = LocalDate.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }

    public BigDecimal getTarifaDiaria() {
        return tarifaDiaria;
    }

    public void setTarifaDiaria(BigDecimal tarifaDiaria) {
        this.tarifaDiaria = tarifaDiaria;
    }

    public int getDiasRetrasoFinales() {
        return diasRetrasoFinales;
    }

    public void setDiasRetrasoFinales(int diasRetrasoFinales) {
        this.diasRetrasoFinales = diasRetrasoFinales;
    }

    public BigDecimal getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(BigDecimal importeTotal) {
        this.importeTotal = importeTotal;
    }

    public LocalDate getFechaCalculo() {
        return fechaCalculo;
    }

    public void setFechaCalculo(LocalDate fechaCalculo) {
        this.fechaCalculo = fechaCalculo;
    }

    public EstadoMulta getEstado() {
        return estado;
    }

    public void setEstado(EstadoMulta estado) {
        this.estado = estado;
    }
}