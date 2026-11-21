package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.EstadoPrestamo;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarPrestamoDevueltoOutboxAdapterTest {

    @Mock
    private OutboxEventRepository repository;

    private RegistrarPrestamoDevueltoOutboxAdapter adapter;

    private final Instant instanteFijo =
            Instant.parse("2026-11-19T15:00:00Z");

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(instanteFijo, ZoneOffset.UTC);
        adapter = new RegistrarPrestamoDevueltoOutboxAdapter(repository, clock);
    }

    @Test
    void noCreaEventoSinIdentidadPersistida() {
        // Arrange
        Prestamo prestamo = Prestamo.crearPendiente(
                new ClienteId(25L),
                new LibroId(80L),
                new ReservaId("reserva-150"),
                LocalDate.of(2026, 10, 1)
        );

        // Act y Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.publicar(prestamo)
        );

        verify(repository, never()).save(any(OutboxEventEntity.class));
        verify(repository, never()).findByEventId(any(UUID.class));
    }

    @Test
    void creaPayloadV1ConIdentificadoresNumericos() {
        // Arrange
        LocalDate fechaPrestamo = LocalDate.of(2026, 10, 1);
        LocalDate fechaLimite = LocalDate.of(2026, 10, 16);
        LocalDate fechaDevolucion = LocalDate.of(2026, 10, 10);

        Prestamo prestamo = Prestamo.rehidratar(
                new PrestamoId(150L),
                new ClienteId(25L),
                new LibroId(80L),
                new ReservaId("reserva-150"),
                fechaPrestamo,
                fechaLimite,
                fechaDevolucion,
                EstadoPrestamo.DEVUELTO
        );

        when(repository.findByEventId(any(UUID.class)))
                .thenReturn(Optional.empty());

        // Act
        adapter.publicar(prestamo);

        // Assert
        verify(repository).save(any(OutboxEventEntity.class));

        org.mockito.ArgumentCaptor<OutboxEventEntity> captor =
                org.mockito.ArgumentCaptor.forClass(OutboxEventEntity.class);

        verify(repository).save(captor.capture());

        OutboxEventEntity evento = captor.getValue();

        UUID eventId = UUID.nameUUIDFromBytes(
                "PrestamoDevuelto:150:2026-10-10".getBytes(
                        java.nio.charset.StandardCharsets.UTF_8
                )
        );

        assertEquals(eventId, evento.getEventId());
        assertEquals("PrestamoDevuelto", evento.getEventType());
        assertEquals(1, evento.getEventVersion());
        assertEquals(150L, evento.getAggregateId());
        assertEquals(OutboxStatus.PENDIENTE, evento.getStatus());
        assertEquals(0, evento.getAttempts());
        assertEquals(instanteFijo, evento.getCreatedAt());
        assertEquals(instanteFijo, evento.getAvailableAt());

        String payload = evento.getPayload();

        assertEquals(
                "{\"eventId\":\"" + eventId
                        + "\",\"eventType\":\"PrestamoDevuelto\""
                        + ",\"eventVersion\":1"
                        + ",\"occurredAt\":\"2026-11-19T15:00:00Z\""
                        + ",\"prestamoId\":150"
                        + ",\"clienteId\":25"
                        + ",\"libroId\":80"
                        + ",\"fechaPrestamo\":\"2026-10-01\""
                        + ",\"fechaLimite\":\"2026-10-16\""
                        + ",\"fechaDevolucion\":\"2026-10-10\"}",
                payload
        );
    }

    @Test
    void unaSegundaInsercionLogicaEsIdempotente() {
        // Arrange
        LocalDate fechaPrestamo = LocalDate.of(2026, 10, 1);
        LocalDate fechaDevolucion = LocalDate.of(2026, 10, 10);

        Prestamo prestamo = Prestamo.rehidratar(
                new PrestamoId(150L),
                new ClienteId(25L),
                new LibroId(80L),
                new ReservaId("reserva-150"),
                fechaPrestamo,
                LocalDate.of(2026, 10, 16),
                fechaDevolucion,
                EstadoPrestamo.DEVUELTO
        );

        UUID eventId = UUID.nameUUIDFromBytes(
                "PrestamoDevuelto:150:2026-10-10".getBytes(
                        java.nio.charset.StandardCharsets.UTF_8
                )
        );

        when(repository.findByEventId(eventId))
                .thenReturn(Optional.of(new OutboxEventEntity()));

        // Act
        adapter.publicar(prestamo);

        // Assert
        verify(repository).findByEventId(eventId);
        verify(repository, never()).save(any(OutboxEventEntity.class));
    }
}