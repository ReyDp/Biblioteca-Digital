/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.application.service;

import com.ingsoft.prestamos.application.port.input.ConsultarPrestamoUseCase;
import com.ingsoft.prestamos.application.port.output.PrestamoPersistencePort;
import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ConsultarPrestamoService
implements ConsultarPrestamoUseCase {
    private final PrestamoPersistencePort prestamoPersistence;

    public ConsultarPrestamoService(PrestamoPersistencePort prestamoPersistence) {
        this.prestamoPersistence = Objects.requireNonNull(prestamoPersistence);
    }

    @Override
    public Optional<Prestamo> buscarPorId(PrestamoId prestamoId) {
        return this.prestamoPersistence.buscarPorId(prestamoId);
    }

    @Override
    public List<Prestamo> buscarPorCliente(ClienteId clienteId) {
        return List.copyOf(this.prestamoPersistence.buscarPorCliente(clienteId));
    }
}

