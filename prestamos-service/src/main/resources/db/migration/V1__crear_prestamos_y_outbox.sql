CREATE SEQUENCE prestamo_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE prestamo (
    id BIGINT PRIMARY KEY DEFAULT nextval('prestamo_id_seq'),
    cliente_id BIGINT NOT NULL,
    libro_id BIGINT NOT NULL,
    reserva_id VARCHAR(120) NOT NULL,
    fecha_prestamo DATE NOT NULL,
    fecha_limite DATE NOT NULL,
    fecha_devolucion DATE,
    estado VARCHAR(32) NOT NULL,
    CONSTRAINT ck_prestamo_fechas CHECK (fecha_limite >= fecha_prestamo),
    CONSTRAINT ck_prestamo_estado CHECK (estado IN ('PENDIENTE_CONFIRMACION', 'ACTIVO', 'DEVUELTO', 'COMPENSACION_REQUERIDA')),
    CONSTRAINT ck_prestamo_devolucion_estado CHECK (
        (estado = 'DEVUELTO' AND fecha_devolucion IS NOT NULL)
        OR (estado <> 'DEVUELTO' AND fecha_devolucion IS NULL)
    )
);

ALTER SEQUENCE prestamo_id_seq OWNED BY prestamo.id;

CREATE UNIQUE INDEX uk_prestamo_libro_ocupado
    ON prestamo (libro_id)
    WHERE estado IN ('PENDIENTE_CONFIRMACION', 'ACTIVO', 'COMPENSACION_REQUERIDA');

CREATE TABLE outbox_event (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    event_version INTEGER NOT NULL,
    aggregate_id BIGINT NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    available_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    last_error VARCHAR(2000),
    CONSTRAINT uk_outbox_event_event_id UNIQUE (event_id),
    CONSTRAINT ck_outbox_status CHECK (status IN ('PENDIENTE', 'PUBLICANDO', 'PUBLICADO', 'ERROR_REINTENTABLE', 'ERROR_PERMANENTE'))
);

CREATE INDEX idx_outbox_pending ON outbox_event (status, available_at);
