/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.application.port.output;

import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import java.util.List;
import java.util.Optional;

public interface PrestamoPersistencePort {
    public Prestamo guardar(Prestamo var1);

    public Optional<Prestamo> buscarPorId(PrestamoId var1);

    public Optional<Prestamo> buscarPorIdConBloqueo(PrestamoId var1);

    public List<Prestamo> buscarPorCliente(ClienteId var1);

    public boolean existePrestamoQueOcupaLibro(LibroId var1);

    public List<Prestamo> buscarPendientesDeReconciliacion();
}

