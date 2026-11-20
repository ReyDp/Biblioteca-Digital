/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestHeader
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 *  tools.jackson.core.JacksonException
 *  tools.jackson.databind.ObjectMapper
 */
package com.ingsoft.prestamos.infrastructure.adapter.in.rest;

import com.ingsoft.prestamos.application.port.input.RegistrarDevolucionUseCase;
import com.ingsoft.prestamos.application.port.input.RegistrarPrestamoUseCase;
import com.ingsoft.prestamos.application.port.output.IdempotenciaPort;
import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import com.ingsoft.prestamos.infrastructure.adapter.in.rest.dto.PrestamoResponse;
import com.ingsoft.prestamos.infrastructure.adapter.in.rest.dto.RegistrarPrestamoRequest;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.JpaIdempotenciaAdapter;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(value={"/api/prestamos"})
public class PrestamoRestController {
    private final RegistrarPrestamoUseCase registrarPrestamoUseCase;
    private final RegistrarDevolucionUseCase registrarDevolucionUseCase;
    private final IdempotenciaPort idempotenciaPort;
    private final ObjectMapper objectMapper;

    public PrestamoRestController(RegistrarPrestamoUseCase registrarPrestamoUseCase, RegistrarDevolucionUseCase registrarDevolucionUseCase, IdempotenciaPort idempotenciaPort, ObjectMapper objectMapper) {
        this.registrarPrestamoUseCase = registrarPrestamoUseCase;
        this.registrarDevolucionUseCase = registrarDevolucionUseCase;
        this.idempotenciaPort = idempotenciaPort;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<PrestamoResponse> registrarPrestamo(@RequestHeader(value="Idempotency-Key") String idempotencyKey, @Valid @RequestBody RegistrarPrestamoRequest request) {
        this.validarIdempotencyKey(idempotencyKey);
        String hash = JpaIdempotenciaAdapter.computeHash(request.clienteId() + ":" + request.libroId());
        PrestamoResponse response = this.ejecutarConIdempotencia("registrar_prestamo", idempotencyKey, hash, () -> {
            Prestamo prestamo = this.registrarPrestamoUseCase.registrar(new ClienteId(request.clienteId()), new LibroId(request.libroId()));
            return PrestamoResponse.desde(prestamo);
        });
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value={"/{id}/devolucion"})
    public ResponseEntity<PrestamoResponse> registrarDevolucion(@PathVariable Long id, @RequestHeader(value="Idempotency-Key") String idempotencyKey) {
        this.validarIdempotencyKey(idempotencyKey);
        String hash = JpaIdempotenciaAdapter.computeHash(String.valueOf(id));
        PrestamoResponse response = this.ejecutarConIdempotencia("registrar_devolucion", idempotencyKey, hash, () -> {
            Prestamo prestamo = this.registrarDevolucionUseCase.registrar(new PrestamoId(id));
            return PrestamoResponse.desde(prestamo);
        });
        return ResponseEntity.ok(response);
    }

    private void validarIdempotencyKey(String key) {
        if (key == null || key.length() < 16 || key.length() > 128) {
            throw new IllegalArgumentException("El encabezado Idempotency-Key es obligatorio y debe tener entre 16 y 128 caracteres.");
        }
    }

    private PrestamoResponse ejecutarConIdempotencia(String operacion, String clave, String hashSolicitud, Supplier<PrestamoResponse> accion) {
        Optional<IdempotenciaPort.Resultado> existente = this.idempotenciaPort.buscar(operacion, clave, hashSolicitud);
        if (existente.isPresent()) {
            IdempotenciaPort.Resultado resultado = existente.get();
            if (!resultado.hashSolicitud().equals(hashSolicitud)) {
                throw new IdempotencyConflictException("La clave de idempotencia se reutiliz\u00f3 con una solicitud diferente.");
            }
            if (resultado.estado() == IdempotenciaPort.Resultado.Estado.COMPLETADA && resultado.respuesta() != null) {
                return this.deserializeResponse(resultado.respuesta());
            }
            throw new IdempotencyConflictException("La operaci\u00f3n con esta clave de idempotencia ya est\u00e1 en proceso. Intente m\u00e1s tarde.");
        }
        this.idempotenciaPort.registrarEnProceso(operacion, clave, hashSolicitud);
        PrestamoResponse response = accion.get();
        String serialized = this.serializeResponse(response);
        this.idempotenciaPort.completar(operacion, clave, hashSolicitud, serialized);
        return response;
    }

    private String serializeResponse(PrestamoResponse response) {
        try {
            return this.objectMapper.writeValueAsString((Object)response);
        }
        catch (JacksonException e) {
            throw new IllegalStateException("No se pudo serializar la respuesta idempotente.", e);
        }
    }

    private PrestamoResponse deserializeResponse(String serialized) {
        try {
            return (PrestamoResponse)this.objectMapper.readValue(serialized, PrestamoResponse.class);
        }
        catch (JacksonException e) {
            throw new IllegalStateException("No se pudo deserializar la respuesta idempotente.", e);
        }
    }

    public static class IdempotencyConflictException
    extends RuntimeException {
        public IdempotencyConflictException(String message) {
            super(message);
        }
    }
}

