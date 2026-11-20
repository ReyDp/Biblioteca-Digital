/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.domain.exception;

public class ClienteNoExisteException
extends RuntimeException {
    public ClienteNoExisteException() {
        super("El cliente no existe.");
    }
}

