/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.application.service;

import com.ingsoft.prestamos.application.port.input.RegistrarPrestamoUseCase;
import com.ingsoft.prestamos.application.port.output.ClienteQueryPort;
import com.ingsoft.prestamos.application.port.output.PrestamoPersistencePort;
import com.ingsoft.prestamos.application.port.output.ReservaLibroPort;
import com.ingsoft.prestamos.domain.exception.ClienteNoExisteException;
import com.ingsoft.prestamos.domain.exception.LibroNoDisponibleException;
import com.ingsoft.prestamos.domain.exception.ReservaNoConfirmadaException;
import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.ReservaLibro;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

public final class RegistrarPrestamoService
implements RegistrarPrestamoUseCase {
    private final PrestamoPersistencePort prestamoPersistence;
    private final ClienteQueryPort clienteQuery;
    private final ReservaLibroPort reservaLibro;
    private final Clock clock;

    public RegistrarPrestamoService(PrestamoPersistencePort prestamoPersistence, ClienteQueryPort clienteQuery, ReservaLibroPort reservaLibro, Clock clock) {
        this.prestamoPersistence = Objects.requireNonNull(prestamoPersistence);
        this.clienteQuery = Objects.requireNonNull(clienteQuery);
        this.reservaLibro = Objects.requireNonNull(reservaLibro);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public Prestamo registrar(ClienteId clienteId, LibroId libroId) {
        Objects.requireNonNull(clienteId, "El cliente es obligatorio.");
        Objects.requireNonNull(libroId, "El libro es obligatorio.");
        if (!this.clienteQuery.existe(clienteId)) {
            throw new ClienteNoExisteException();
        }
        if (this.prestamoPersistence.existePrestamoQueOcupaLibro(libroId)) {
            throw new LibroNoDisponibleException();
        }
        ReservaLibro reserva = this.reservaLibro.solicitarReserva(libroId);
        if (!reserva.puedeConfirmarse() && !reserva.estaConfirmada()) {
            throw new LibroNoDisponibleException();
        }
        Prestamo pendiente = this.prestamoPersistence.guardar(Prestamo.crearPendiente(clienteId, libroId, reserva.reservaId(), LocalDate.now(this.clock)));
        ReservaLibro confirmada = this.reservaLibro.confirmarReserva(reserva.reservaId());
        if (!confirmada.estaConfirmada()) {
            pendiente.marcarCompensacionRequerida();
            this.prestamoPersistence.guardar(pendiente);
            throw new ReservaNoConfirmadaException();
        }
        pendiente.confirmarReserva();
        return this.prestamoPersistence.guardar(pendiente);
    }
}

