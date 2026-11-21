/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxEventEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository
extends JpaRepository<OutboxEventEntity, Long> {
    public Optional<OutboxEventEntity> findByEventId(UUID var1);

    @Query(value="SELECT id FROM outbox_event\nWHERE (status IN ('PENDIENTE', 'ERROR_REINTENTABLE') AND available_at <= :now)\n   OR (status = 'PUBLICANDO' AND publishing_since <= :expiredBefore)\nORDER BY created_at\nFOR UPDATE SKIP LOCKED\nLIMIT :batchSize\n", nativeQuery=true)
    public List<Long> lockPublishableIds(@Param(value="now") Instant var1, @Param(value="expiredBefore") Instant var2, @Param(value="batchSize") int var3);
}

