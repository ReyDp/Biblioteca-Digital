/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Entity
 *  jakarta.persistence.EnumType
 *  jakarta.persistence.Enumerated
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.SequenceGenerator
 *  jakarta.persistence.Table
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

import com.ingsoft.prestamos.domain.model.EstadoPrestamo;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name="prestamo")
public class PrestamoEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="prestamo_id_generator")
    @SequenceGenerator(name="prestamo_id_generator", sequenceName="prestamo_id_seq", allocationSize=1)
    private Long id;
    private Long clienteId;
    private Long libroId;
    private String reservaId;
    private LocalDate fechaPrestamo;
    private LocalDate fechaLimite;
    private LocalDate fechaDevolucion;
    @Enumerated(value=EnumType.STRING)
    private EstadoPrestamo estado;

    protected PrestamoEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getLibroId() {
        return this.libroId;
    }

    public void setLibroId(Long libroId) {
        this.libroId = libroId;
    }

    public String getReservaId() {
        return this.reservaId;
    }

    public void setReservaId(String reservaId) {
        this.reservaId = reservaId;
    }

    public LocalDate getFechaPrestamo() {
        return this.fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaLimite() {
        return this.fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public LocalDate getFechaDevolucion() {
        return this.fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public EstadoPrestamo getEstado() {
        return this.estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }
}

