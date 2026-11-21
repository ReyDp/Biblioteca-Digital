/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

public enum OutboxStatus {
    PENDIENTE,
    PUBLICANDO,
    PUBLICADO,
    ERROR_REINTENTABLE,
    ERROR_PERMANENTE;

}

