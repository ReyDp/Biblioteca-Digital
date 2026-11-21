/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.application.port.input;

import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import java.util.List;
import java.util.Optional;

public interface ConsultarPrestamoUseCase {
    public Optional<Prestamo> buscarPorId(PrestamoId var1);

    public List<Prestamo> buscarPorCliente(ClienteId var1);
}

