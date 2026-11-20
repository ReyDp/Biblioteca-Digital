/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.converter.HttpMessageNotReadableException
 *  org.springframework.web.bind.MethodArgumentNotValidException
 *  org.springframework.web.bind.annotation.ExceptionHandler
 *  org.springframework.web.bind.annotation.RestControllerAdvice
 */
package com.ingsoft.prestamos.infrastructure.adapter.in.rest;

import com.ingsoft.prestamos.domain.exception.ClienteNoExisteException;
import com.ingsoft.prestamos.domain.exception.LibroNoDisponibleException;
import com.ingsoft.prestamos.domain.exception.PrestamoEstadoNoPermitidoException;
import com.ingsoft.prestamos.domain.exception.PrestamoNoExisteException;
import com.ingsoft.prestamos.domain.exception.PrestamoYaDevueltoException;
import com.ingsoft.prestamos.domain.exception.ReconciliacionRequeridaException;
import com.ingsoft.prestamos.domain.exception.ReglaPrestamoInvalidaException;
import com.ingsoft.prestamos.domain.exception.ReservaNoConfirmadaException;
import com.ingsoft.prestamos.infrastructure.adapter.in.rest.PrestamoRestController;
import com.ingsoft.prestamos.infrastructure.adapter.in.rest.dto.ApiErrorResponse;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.JpaIdempotenciaAdapter;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes={PrestamoRestController.class})
public class PrestamoRestExceptionHandler {
    @ExceptionHandler(value={PrestamoRestController.IdempotencyConflictException.class})
    public ResponseEntity<ApiErrorResponse> manejar(PrestamoRestController.IdempotencyConflictException ex) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CONFLICT).body(ApiErrorResponse.de("IDEMPOTENCIA_CONFLICTO", ex.getMessage()));
    }

    @ExceptionHandler(value={JpaIdempotenciaAdapter.IdempotencyConflictException.class})
    public ResponseEntity<ApiErrorResponse> manejar(JpaIdempotenciaAdapter.IdempotencyConflictException ex) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CONFLICT).body(ApiErrorResponse.de("IDEMPOTENCIA_CONFLICTO", ex.getMessage()));
    }

    @ExceptionHandler(value={ClienteNoExisteException.class})
    public ResponseEntity<ApiErrorResponse> manejar(ClienteNoExisteException ex) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.NOT_FOUND).body(ApiErrorResponse.de("CLIENTE_NO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(value={PrestamoNoExisteException.class})
    public ResponseEntity<ApiErrorResponse> manejar(PrestamoNoExisteException ex) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.NOT_FOUND).body(ApiErrorResponse.de("PRESTAMO_NO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(value={LibroNoDisponibleException.class})
    public ResponseEntity<ApiErrorResponse> manejar(LibroNoDisponibleException ex) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CONFLICT).body(ApiErrorResponse.de("LIBRO_NO_DISPONIBLE", ex.getMessage()));
    }

    @ExceptionHandler(value={PrestamoYaDevueltoException.class})
    public ResponseEntity<ApiErrorResponse> manejar(PrestamoYaDevueltoException ex) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CONFLICT).body(ApiErrorResponse.de("PRESTAMO_YA_DEVUELTO", ex.getMessage()));
    }

    @ExceptionHandler(value={PrestamoEstadoNoPermitidoException.class})
    public ResponseEntity<ApiErrorResponse> manejar(PrestamoEstadoNoPermitidoException ex) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CONFLICT).body(ApiErrorResponse.de("ESTADO_NO_PERMITIDO", ex.getMessage()));
    }

    @ExceptionHandler(value={ReconciliacionRequeridaException.class})
    public ResponseEntity<ApiErrorResponse> manejar(ReconciliacionRequeridaException ex) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CONFLICT).body(ApiErrorResponse.de("RECONCILIACION_REQUERIDA", ex.getMessage()));
    }

    @ExceptionHandler(value={ReservaNoConfirmadaException.class})
    public ResponseEntity<ApiErrorResponse> manejar(ReservaNoConfirmadaException ex) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CONFLICT).body(ApiErrorResponse.de("RESERVA_NO_CONFIRMADA", ex.getMessage()));
    }

    @ExceptionHandler(value={ReglaPrestamoInvalidaException.class})
    public ResponseEntity<ApiErrorResponse> manejar(ReglaPrestamoInvalidaException ex) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.BAD_REQUEST).body(ApiErrorResponse.de("REGLA_PRESTAMO_INVALIDA", ex.getMessage()));
    }

    @ExceptionHandler(value={MethodArgumentNotValidException.class})
    public ResponseEntity<ApiErrorResponse> manejar(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiErrorResponse.de("VALIDACION_FALLIDA", detalle));
    }

    @ExceptionHandler(value={HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorResponse> manejar(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.de("JSON_INVALIDO", "El cuerpo de la solicitud no es JSON v\u00e1lido."));
    }

    @ExceptionHandler(value={IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> manejar(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.de("ARGUMENTO_INVALIDO", ex.getMessage()));
    }
}

