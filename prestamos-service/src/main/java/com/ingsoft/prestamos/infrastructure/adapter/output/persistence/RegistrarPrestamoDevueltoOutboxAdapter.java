package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

import com.ingsoft.prestamos.application.port.output.PublicarPrestamoDevueltoPort;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxEventEntity;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxEventRepository;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxStatus;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RegistrarPrestamoDevueltoOutboxAdapter
implements PublicarPrestamoDevueltoPort {
    private static final String EVENT_TYPE = "PrestamoDevuelto";
    private static final int EVENT_VERSION = 1;
    private final OutboxEventRepository repository;
    private final Clock clock;

    public RegistrarPrestamoDevueltoOutboxAdapter(OutboxEventRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void publicar(Prestamo prestamo) {
        long prestamoId = prestamo.id().orElseThrow(() -> new IllegalArgumentException("No se puede crear un evento sin ID persistido.")).value();
        if (prestamo.fechaDevolucion() == null) {
            throw new IllegalArgumentException("No se puede crear un evento sin fecha de devoluci\u00f3n.");
        }
        UUID eventId = UUID.nameUUIDFromBytes(("PrestamoDevuelto:" + prestamoId + ":" + String.valueOf(prestamo.fechaDevolucion())).getBytes(StandardCharsets.UTF_8));
        if (this.repository.findByEventId(eventId).isPresent()) {
            return;
        }
        Instant now = Instant.now(this.clock);
        OutboxEventEntity event = new OutboxEventEntity();
        event.setEventId(eventId);
        event.setEventType(EVENT_TYPE);
        event.setEventVersion(1);
        event.setAggregateId(prestamoId);
        event.setPayload(this.payload(eventId, now, prestamoId, prestamo));
        event.setStatus(OutboxStatus.PENDIENTE);
        event.setAttempts(0);
        event.setCreatedAt(now);
        event.setAvailableAt(now);
        this.repository.save(event);
    }

    private String payload(UUID eventId, Instant occurredAt, long prestamoId, Prestamo prestamo) {
        return "{\"eventId\":\"" + String.valueOf(eventId) + "\",\"eventType\":\"PrestamoDevuelto\",\"eventVersion\":1,\"occurredAt\":\"" + DateTimeFormatter.ISO_INSTANT.format(occurredAt) + "\",\"prestamoId\":" + prestamoId + ",\"clienteId\":" + prestamo.clienteId().value() + ",\"libroId\":" + prestamo.libroId().value() + ",\"fechaPrestamo\":\"" + String.valueOf(prestamo.fechaPrestamo()) + "\",\"fechaLimite\":\"" + String.valueOf(prestamo.fechaLimite()) + "\",\"fechaDevolucion\":\"" + String.valueOf(prestamo.fechaDevolucion()) + "\"}";
    }
}

