/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.domain.exception;

public class ReservaNoConfirmadaException
extends RuntimeException {
    public ReservaNoConfirmadaException() {
        super("La reserva no fue confirmada.");
    }
}

