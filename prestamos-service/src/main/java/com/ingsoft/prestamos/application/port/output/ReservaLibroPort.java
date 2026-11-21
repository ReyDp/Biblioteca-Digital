/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.application.port.output;

import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import com.ingsoft.prestamos.domain.model.ReservaLibro;
import java.util.Optional;

public interface ReservaLibroPort {
    public ReservaLibro solicitarReserva(LibroId var1);

    public Optional<ReservaLibro> consultarReserva(ReservaId var1);

    public ReservaLibro confirmarReserva(ReservaId var1);

    public ReservaLibro cancelarReserva(ReservaId var1);
}

