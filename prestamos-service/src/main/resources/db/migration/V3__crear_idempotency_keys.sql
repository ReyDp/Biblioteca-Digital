CREATE TABLE idempotency_keys (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    response_body TEXT,
    CONSTRAINT uk_idempotency_key UNIQUE (idempotency_key)
);
