/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.IdempotencyKeyEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository
extends JpaRepository<IdempotencyKeyEntity, Long> {
    public Optional<IdempotencyKeyEntity> findByIdempotencyKey(String var1);
}

