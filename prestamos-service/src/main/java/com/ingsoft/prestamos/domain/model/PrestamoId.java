/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.domain.model;

public record PrestamoId(Long value) {
    public PrestamoId {
        if (value == null || value <= 0L) {
            throw new IllegalArgumentException("El identificador del pr\u00e9stamo debe ser positivo.");
        }
    }

    public static PrestamoId asignado(Long value) {
        return new PrestamoId(value);
    }
}

