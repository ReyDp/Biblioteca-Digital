/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.domain.model;

import java.util.Objects;

public record ReservaId(String value) {
    public ReservaId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador de la reserva es obligatorio.");
        }
        value = value.trim();
        Objects.requireNonNull(value);
    }
}

