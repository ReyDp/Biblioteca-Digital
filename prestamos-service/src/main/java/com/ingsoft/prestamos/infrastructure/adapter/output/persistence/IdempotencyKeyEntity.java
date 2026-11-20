/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.Table
 *  jakarta.persistence.UniqueConstraint
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name="idempotency_keys", uniqueConstraints={@UniqueConstraint(name="uk_idempotency_key", columnNames={"idempotency_key"})})
public class IdempotencyKeyEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="idempotency_key", nullable=false, updatable=false, length=128)
    private String idempotencyKey;
    @Column(name="operation", nullable=false, updatable=false, length=50)
    private String operation;
    @Column(name="request_hash", nullable=false, updatable=false, length=64)
    private String requestHash;
    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;
    @Column(name="response_body", length=4096)
    private String responseBody;

    protected IdempotencyKeyEntity() {
    }

    public IdempotencyKeyEntity(String idempotencyKey, String operation, String requestHash, Instant createdAt) {
        this.idempotencyKey = idempotencyKey;
        this.operation = operation;
        this.requestHash = requestHash;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return this.id;
    }

    public String getIdempotencyKey() {
        return this.idempotencyKey;
    }

    public String getOperation() {
        return this.operation;
    }

    public String getRequestHash() {
        return this.requestHash;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}

