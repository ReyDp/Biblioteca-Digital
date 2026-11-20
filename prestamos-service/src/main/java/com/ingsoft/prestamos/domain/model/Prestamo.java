/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.domain.model;

import com.ingsoft.prestamos.domain.exception.ConfirmacionPendienteException;
import com.ingsoft.prestamos.domain.exception.PrestamoEstadoNoPermitidoException;
import com.ingsoft.prestamos.domain.exception.PrestamoYaDevueltoException;
import com.ingsoft.prestamos.domain.exception.ReconciliacionRequeridaException;
import com.ingsoft.prestamos.domain.exception.ReglaPrestamoInvalidaException;
import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.EstadoPrestamo;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public final class Prestamo {
    public static final int DURACION_DIAS = 15;
    private final PrestamoId id;
    private final ClienteId clienteId;
    private final LibroId libroId;
    private final ReservaId reservaId;
    private final LocalDate fechaPrestamo;
    private final LocalDate fechaLimite;
    private LocalDate fechaDevolucion;
    private EstadoPrestamo estado;

    private Prestamo(PrestamoId id, ClienteId clienteId, LibroId libroId, ReservaId reservaId, LocalDate fechaPrestamo, LocalDate fechaLimite, LocalDate fechaDevolucion, EstadoPrestamo estado) {
        this.id = id;
        this.clienteId = Objects.requireNonNull(clienteId, "El cliente es obligatorio.");
        this.libroId = Objects.requireNonNull(libroId, "El libro es obligatorio.");
        this.reservaId = Objects.requireNonNull(reservaId, "La reserva es obligatoria.");
        this.fechaPrestamo = Objects.requireNonNull(fechaPrestamo, "La fecha de pr\u00e9stamo es obligatoria.");
        this.fechaLimite = Objects.requireNonNull(fechaLimite, "La fecha l\u00edmite es obligatoria.");
        this.fechaDevolucion = fechaDevolucion;
        this.estado = Objects.requireNonNull(estado, "El estado es obligatorio.");
        this.validarFechas();
        this.validarEstado();
    }

    public static Prestamo crearPendiente(ClienteId clienteId, LibroId libroId, ReservaId reservaId, LocalDate fechaPrestamo) {
        Objects.requireNonNull(fechaPrestamo, "La fecha de pr\u00e9stamo es obligatoria.");
        return new Prestamo(null, clienteId, libroId, reservaId, fechaPrestamo, fechaPrestamo.plusDays(15L), null, EstadoPrestamo.PENDIENTE_CONFIRMACION);
    }

    public static Prestamo rehidratar(PrestamoId id, ClienteId clienteId, LibroId libroId, ReservaId reservaId, LocalDate fechaPrestamo, LocalDate fechaLimite, LocalDate fechaDevolucion, EstadoPrestamo estado) {
        Objects.requireNonNull(id, "El identificador del pr\u00e9stamo es obligatorio al rehidratar.");
        return new Prestamo(id, clienteId, libroId, reservaId, fechaPrestamo, fechaLimite, fechaDevolucion, estado);
    }

    public Prestamo conId(PrestamoId id) {
        Objects.requireNonNull(id, "El identificador del pr\u00e9stamo es obligatorio.");
        if (this.id != null && !this.id.equals(id)) {
            throw new IllegalStateException("La identidad del pr\u00e9stamo no puede cambiar.");
        }
        return new Prestamo(id, this.clienteId, this.libroId, this.reservaId, this.fechaPrestamo, this.fechaLimite, this.fechaDevolucion, this.estado);
    }

    public void confirmarReserva() {
        if (this.estado != EstadoPrestamo.PENDIENTE_CONFIRMACION) {
            throw new PrestamoEstadoNoPermitidoException("Solo un pr\u00e9stamo pendiente puede activarse.");
        }
        this.estado = EstadoPrestamo.ACTIVO;
    }

    public void marcarCompensacionRequerida() {
        if (this.estado != EstadoPrestamo.PENDIENTE_CONFIRMACION) {
            throw new ReconciliacionRequeridaException("Solo un pr\u00e9stamo pendiente requiere compensaci\u00f3n.");
        }
        this.estado = EstadoPrestamo.COMPENSACION_REQUERIDA;
    }

    public void registrarDevolucion(LocalDate fechaDevolucion) {
        Objects.requireNonNull(fechaDevolucion, "La fecha de devoluci\u00f3n es obligatoria.");
        if (this.estado == EstadoPrestamo.DEVUELTO) {
            throw new PrestamoYaDevueltoException();
        }
        if (this.estado == EstadoPrestamo.PENDIENTE_CONFIRMACION) {
            throw new ConfirmacionPendienteException("El pr\u00e9stamo a\u00fan no tiene confirmada la reserva.");
        }
        if (this.estado == EstadoPrestamo.COMPENSACION_REQUERIDA) {
            throw new ReconciliacionRequeridaException("El pr\u00e9stamo requiere reconciliaci\u00f3n antes de devolverse.");
        }
        if (fechaDevolucion.isBefore(this.fechaPrestamo)) {
            throw new ReglaPrestamoInvalidaException("La fecha de devoluci\u00f3n no puede preceder a la fecha de pr\u00e9stamo.");
        }
        this.fechaDevolucion = fechaDevolucion;
        this.estado = EstadoPrestamo.DEVUELTO;
    }

    public boolean esActivo() {
        return this.estado == EstadoPrestamo.ACTIVO;
    }

    public Optional<PrestamoId> id() {
        return Optional.ofNullable(this.id);
    }

    public ClienteId clienteId() {
        return this.clienteId;
    }

    public LibroId libroId() {
        return this.libroId;
    }

    public ReservaId reservaId() {
        return this.reservaId;
    }

    public LocalDate fechaPrestamo() {
        return this.fechaPrestamo;
    }

    public LocalDate fechaLimite() {
        return this.fechaLimite;
    }

    public LocalDate fechaDevolucion() {
        return this.fechaDevolucion;
    }

    public EstadoPrestamo estado() {
        return this.estado;
    }

    private void validarFechas() {
        if (this.fechaLimite.isBefore(this.fechaPrestamo)) {
            throw new ReglaPrestamoInvalidaException("La fecha l\u00edmite no puede preceder a la fecha de pr\u00e9stamo.");
        }
        if (this.fechaDevolucion != null && this.fechaDevolucion.isBefore(this.fechaPrestamo)) {
            throw new ReglaPrestamoInvalidaException("La fecha de devoluci\u00f3n no puede preceder a la fecha de pr\u00e9stamo.");
        }
    }

    private void validarEstado() {
        if (this.estado == EstadoPrestamo.DEVUELTO && this.fechaDevolucion == null) {
            throw new ReglaPrestamoInvalidaException("Un pr\u00e9stamo devuelto debe tener fecha de devoluci\u00f3n.");
        }
        if (this.estado != EstadoPrestamo.DEVUELTO && this.fechaDevolucion != null) {
            throw new ReglaPrestamoInvalidaException("Solo un pr\u00e9stamo devuelto puede tener fecha de devoluci\u00f3n.");
        }
    }
}

