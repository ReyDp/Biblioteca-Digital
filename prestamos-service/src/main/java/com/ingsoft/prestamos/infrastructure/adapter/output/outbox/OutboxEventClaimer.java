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
import java.time.Instant;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class OutboxEventClaimer {
    private final OutboxEventRepository repository;
    private final Clock clock;
    private final OutboxPublisherProperties properties;

    public OutboxEventClaimer(OutboxEventRepository repository, Clock clock, OutboxPublisherProperties properties) {
        this.repository = repository;
        this.clock = clock;
        this.properties = properties;
    }

    @Transactional
    public List<OutboxEventEntity> claimNextBatch() {
        Instant now = Instant.now(this.clock);
        List<Long> ids = this.repository.lockPublishableIds(now, now.minus(this.properties.getLeaseDuration()), this.properties.getBatchSize());
        List<OutboxEventEntity> events = this.repository.findAllById(ids);
        events.forEach(event -> {
            event.setStatus(OutboxStatus.PUBLICANDO);
            event.setPublishingSince(now);
            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(null);
        });
        return this.repository.saveAll(events);
    }
}

