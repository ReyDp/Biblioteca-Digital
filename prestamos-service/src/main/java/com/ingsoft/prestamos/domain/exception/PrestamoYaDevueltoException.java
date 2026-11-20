/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.domain.exception;

import com.ingsoft.prestamos.domain.exception.PrestamoEstadoNoPermitidoException;

public class PrestamoYaDevueltoException
extends PrestamoEstadoNoPermitidoException {
    public PrestamoYaDevueltoException() {
        super("El pr\u00e9stamo ya fue devuelto.");
    }
}

