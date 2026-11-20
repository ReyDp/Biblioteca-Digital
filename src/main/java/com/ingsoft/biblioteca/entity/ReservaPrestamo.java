package com.ingsoft.biblioteca.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservas_prestamo")
public class ReservaPrestamo {
    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReservaPrestamo estado;

    protected ReservaPrestamo() { }

    public ReservaPrestamo(String id, Libro libro) {
        this.id = id;
        this.libro = libro;
        this.estado = EstadoReservaPrestamo.VIGENTE;
    }

    public String getId() { return id; }
    public Libro getLibro() { return libro; }
    public EstadoReservaPrestamo getEstado() { return estado; }
    public void setEstado(EstadoReservaPrestamo estado) { this.estado = estado; }
}
