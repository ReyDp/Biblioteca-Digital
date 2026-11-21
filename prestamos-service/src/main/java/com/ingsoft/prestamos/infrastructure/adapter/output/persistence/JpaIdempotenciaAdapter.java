/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.dao.DataIntegrityViolationException
 *  org.springframework.stereotype.Component
 *  org.springframework.transaction.annotation.Transactional
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

import com.ingsoft.prestamos.application.port.output.IdempotenciaPort;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.IdempotencyKeyEntity;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.IdempotencyKeyRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JpaIdempotenciaAdapter
implements IdempotenciaPort {
    private final IdempotencyKeyRepository repository;
    private final Clock clock;

    public JpaIdempotenciaAdapter(IdempotencyKeyRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly=true)
    public Optional<IdempotenciaPort.Resultado> buscar(String operacion, String clave, String hashSolicitud) {
        return this.repository.findByIdempotencyKey(clave).map(entity -> new IdempotenciaPort.Resultado(entity.getRequestHash(), entity.getResponseBody(), entity.getResponseBody() != null ? IdempotenciaPort.Resultado.Estado.COMPLETADA : IdempotenciaPort.Resultado.Estado.EN_PROCESO));
    }

    @Override
    @Transactional
    public void registrarEnProceso(String operacion, String clave, String hashSolicitud) {
        Instant ahora = Instant.now(this.clock);
        try {
            IdempotencyKeyEntity entity = new IdempotencyKeyEntity(clave, operacion, hashSolicitud, ahora);
            this.repository.save(entity);
        }
        catch (DataIntegrityViolationException e) {
            throw new IdempotencyConflictException("La clave de idempotencia '" + clave + "' ya est\u00e1 en uso.", e);
        }
    }

    @Override
    @Transactional
    public void completar(String operacion, String clave, String hashSolicitud, String respuesta) {
        this.repository.findByIdempotencyKey(clave).ifPresent(entity -> entity.setResponseBody(respuesta));
    }

    public static String computeHash(String body) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(body.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    public static class IdempotencyConflictException
    extends RuntimeException {
        public IdempotencyConflictException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

