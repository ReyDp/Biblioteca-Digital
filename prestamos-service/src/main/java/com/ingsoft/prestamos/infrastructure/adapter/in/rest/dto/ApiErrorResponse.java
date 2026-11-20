/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.infrastructure.adapter.in.rest.dto;

public record ApiErrorResponse(String code, String message, String traceId) {
    public static ApiErrorResponse de(String code, String message) {
        return new ApiErrorResponse(code, message, null);
    }

    public static ApiErrorResponse de(String code, String message, String traceId) {
        return new ApiErrorResponse(code, message, traceId);
    }
}

