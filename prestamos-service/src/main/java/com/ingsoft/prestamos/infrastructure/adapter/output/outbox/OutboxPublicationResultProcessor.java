/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.transaction.annotation.Transactional
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.outbox;

import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.OutboxPublisherProperties;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxEventEntity;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxEventRepository;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;
import org.springframework.transaction.annotation.Transactional;

public class OutboxPublicationResultProcessor {
    private final OutboxEventRepository repository;
    private final Clock clock;
    private final OutboxPublisherProperties properties;

    public OutboxPublicationResultProcessor(OutboxEventRepository repository, Clock clock, OutboxPublisherProperties properties) {
        this.repository = repository;
        this.clock = clock;
        this.properties = properties;
    }

    @Transactional
    public void published(OutboxEventEntity event) {
        this.update(event, current -> {
            current.setStatus(OutboxStatus.PUBLICADO);
            current.setPublishedAt(Instant.now(this.clock));
            current.setPublishingSince(null);
            current.setLastError(null);
        });
    }

    @Transactional
    public void failed(OutboxEventEntity event, String error, Duration retryAfter) {
        this.update(event, current -> {
            current.setPublishingSince(null);
            current.setLastError(error.length() > 2000 ? error.substring(0, 2000) : error);
            if (current.getAttempts() >= this.properties.getMaxAttempts()) {
                current.setStatus(OutboxStatus.ERROR_PERMANENTE);
                return;
            }
            current.setStatus(OutboxStatus.ERROR_REINTENTABLE);
            current.setAvailableAt(Instant.now(this.clock).plus(retryAfter == null ? this.backoff(current.getAttempts()) : retryAfter));
        });
    }

    @Transactional
    public void permanentlyFailed(OutboxEventEntity event, String error) {
        this.update(event, current -> {
            current.setStatus(OutboxStatus.ERROR_PERMANENTE);
            current.setPublishingSince(null);
            current.setLastError(error.length() > 2000 ? error.substring(0, 2000) : error);
        });
    }

    private void update(OutboxEventEntity event, Consumer<OutboxEventEntity> updater) {
        this.repository.findById(event.getId()).filter(current -> current.getStatus() == OutboxStatus.PUBLICANDO && Objects.equals(current.getPublishingSince(), event.getPublishingSince())).ifPresent(current -> {
            updater.accept((OutboxEventEntity)current);
            this.repository.save(current);
        });
    }

    private Duration backoff(int attempts) {
        Duration value = this.properties.getInitialBackoff().multipliedBy(1L << Math.min(30, Math.max(0, attempts - 1)));
        return value.compareTo(this.properties.getMaxBackoff()) > 0 ? this.properties.getMaxBackoff() : value;
    }
}

