package com.ingsoft.biblioteca.dto;

import java.math.BigDecimal;

public class DashboardStats {

    private long totalEjemplares;
    private long ejemplaresDisponibles;
    private long ejemplaresInactivos;
    private long prestamosActivos;
    private long prestamosVencidos;
    private long prestamosDevueltos;
    private long multasPendientes;
    private BigDecimal saldoTotalPendiente;

    public long getTotalEjemplares() {
        return totalEjemplares;
    }

    public void setTotalEjemplares(long totalEjemplares) {
        this.totalEjemplares = totalEjemplares;
    }

    public long getEjemplaresDisponibles() {
        return ejemplaresDisponibles;
    }

    public void setEjemplaresDisponibles(long ejemplaresDisponibles) {
        this.ejemplaresDisponibles = ejemplaresDisponibles;
    }

    public long getEjemplaresInactivos() {
        return ejemplaresInactivos;
    }

    public void setEjemplaresInactivos(long ejemplaresInactivos) {
        this.ejemplaresInactivos = ejemplaresInactivos;
    }

    public long getPrestamosActivos() {
        return prestamosActivos;
    }

    public void setPrestamosActivos(long prestamosActivos) {
        this.prestamosActivos = prestamosActivos;
    }

    public long getPrestamosVencidos() {
        return prestamosVencidos;
    }

    public void setPrestamosVencidos(long prestamosVencidos) {
        this.prestamosVencidos = prestamosVencidos;
    }

    public long getPrestamosDevueltos() {
        return prestamosDevueltos;
    }

    public void setPrestamosDevueltos(long prestamosDevueltos) {
        this.prestamosDevueltos = prestamosDevueltos;
    }

    public long getMultasPendientes() {
        return multasPendientes;
    }

    public void setMultasPendientes(long multasPendientes) {
        this.multasPendientes = multasPendientes;
    }

    public BigDecimal getSaldoTotalPendiente() {
        return saldoTotalPendiente;
    }

    public void setSaldoTotalPendiente(BigDecimal saldoTotalPendiente) {
        this.saldoTotalPendiente = saldoTotalPendiente;
    }
}
