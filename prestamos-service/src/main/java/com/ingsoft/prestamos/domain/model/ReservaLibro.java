/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.domain.model;

import com.ingsoft.prestamos.domain.model.EstadoReserva;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import java.util.Objects;

public record ReservaLibro(ReservaId reservaId, LibroId libroId, EstadoReserva estado) {
    public ReservaLibro {
        Objects.requireNonNull(reservaId, "La reserva es obligatoria.");
        Objects.requireNonNull(libroId, "El libro es obligatorio.");
        Objects.requireNonNull(estado, "El estado de la reserva es obligatorio.");
    }

    public boolean puedeConfirmarse() {
        return this.estado == EstadoReserva.VIGENTE || this.estado == EstadoReserva.CONFIRMANDO;
    }

    public boolean estaConfirmada() {
        return this.estado == EstadoReserva.CONFIRMADA;
    }
}

