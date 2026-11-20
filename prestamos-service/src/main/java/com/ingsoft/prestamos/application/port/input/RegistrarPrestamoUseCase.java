/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.application.port.input;

import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;

public interface RegistrarPrestamoUseCase {
    public Prestamo registrar(ClienteId var1, LibroId var2);
}

