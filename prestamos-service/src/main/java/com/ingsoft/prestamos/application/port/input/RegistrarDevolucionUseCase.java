/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.application.port.input;

import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;

public interface RegistrarDevolucionUseCase {
    public Prestamo registrar(PrestamoId var1);
}

