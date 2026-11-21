/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.domain.model;

public record ClienteId(Long value) {
    public ClienteId {
        if (value == null || value < 1L) {
            throw new IllegalArgumentException("El identificador del cliente debe ser positivo.");
        }
    }
}

