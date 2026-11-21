
package com.ingsoft.prestamos.infrastructure.adapter.output.outbox;

import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxEventEntity;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxEventRepository;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxHttpPublisherTest {

    private static final Instant NOW =
            Instant.parse("2026-11-19T15:00:00Z");

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private OutboxEventClaimer claimer;

    @Mock
    private OutboxHttpClient client;

    @Mock
    private HttpResponse<Void> response;

    private Clock clock;
    private OutboxPublisherProperties properties;
    private OutboxPublicationResultProcessor results;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZoneOffset.UTC);

        properties = new OutboxPublisherProperties();
        properties.setMaxAttempts(3);
        properties.setInitialBackoff(Duration.ofSeconds(5));
        properties.setMaxBackoff(Duration.ofMinutes(1));
        properties.setLeaseDuration(Duration.ofMinutes(1));
        properties.setBatchSize(10);

        results = new OutboxPublicationResultProcessor(
                repository, clock, properties
        );
    }

    @Test
    void dosPublicadoresLogicosNoEnvianElMismoEvento() throws Exception {
        OutboxEventEntity event = createEvent(
                1L, OutboxStatus.PENDIENTE, 0, NOW
        );

        when(repository.lockPublishableIds(
                eq(NOW), eq(NOW.minus(Duration.ofMinutes(1))), eq(10)
        )).thenReturn(List.of(1L), List.of());

        when(repository.findAllById(List.of(1L)))
                .thenReturn(List.of(event));

        when(repository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HttpResponse<Void> okResponse = successResponse(204);
        when(client.post(any())).thenReturn(okResponse);
        when(repository.findById(1L)).thenReturn(Optional.of(event));

        OutboxEventClaimer firstClaimer =
                new OutboxEventClaimer(repository, clock, properties);

        OutboxEventClaimer secondClaimer =
                new OutboxEventClaimer(repository, clock, properties);

        OutboxHttpPublisher firstPublisher =
                new OutboxHttpPublisher(firstClaimer, client, results, clock);

        OutboxHttpPublisher secondPublisher =
                new OutboxHttpPublisher(secondClaimer, client, results, clock);

        firstPublisher.publishPendingEvents();
        secondPublisher.publishPendingEvents();

        verify(client, times(1)).post(event.getPayload());
    }

    @Test
    void publica2xxYMarcaPublicado() throws Exception {
        OutboxEventEntity event = createEvent(
                2L, OutboxStatus.PUBLICANDO, 1, NOW
        );

        when(claimer.claimNextBatch()).thenReturn(List.of(event));
        HttpResponse<Void> okResponse = successResponse(200);
        when(client.post(event.getPayload())).thenReturn(okResponse);
        when(repository.findById(2L)).thenReturn(Optional.of(event));

        publisher().publishPendingEvents();

        assertEquals(OutboxStatus.PUBLICADO, event.getStatus());
        assertEquals(NOW, event.getPublishedAt());
        assertEquals(null, event.getPublishingSince());
        assertEquals(null, event.getLastError());

        verify(repository).save(event);
    }

    @Test
    void maximoIntentosMarcaErrorPermanenteSinEliminar() throws Exception {
        OutboxEventEntity event = createEvent(
                3L, OutboxStatus.PUBLICANDO, 3, NOW
        );

        when(repository.findById(3L)).thenReturn(Optional.of(event));

        results.failed(event, "HTTP 500", null);

        assertEquals(OutboxStatus.ERROR_PERMANENTE, event.getStatus());
        assertEquals(null, event.getPublishingSince());
        assertEquals("HTTP 500", event.getLastError());

        verify(repository).save(event);
        verify(repository, never()).delete(any(OutboxEventEntity.class));
    }

    @Test
    void reclamaEventosPublicandoConLeaseVencido() throws Exception {
        OutboxEventEntity event = createEvent(
                4L,
                OutboxStatus.PUBLICANDO,
                1,
                NOW.minus(Duration.ofMinutes(5))
        );

        when(repository.lockPublishableIds(
                NOW,
                NOW.minus(Duration.ofMinutes(1)),
                10
        )).thenReturn(List.of(4L));

        when(repository.findAllById(List.of(4L)))
                .thenReturn(List.of(event));

        when(repository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OutboxEventClaimer eventClaimer =
                new OutboxEventClaimer(repository, clock, properties);

        List<OutboxEventEntity> claimed = eventClaimer.claimNextBatch();

        assertEquals(1, claimed.size());
        assertEquals(OutboxStatus.PUBLICANDO, event.getStatus());
        assertEquals(NOW, event.getPublishingSince());
        assertEquals(2, event.getAttempts());
        assertEquals(null, event.getLastError());

        verify(repository).lockPublishableIds(
                NOW, NOW.minus(Duration.ofMinutes(1)), 10
        );
    }

    @Test
    void timeoutConservaEventoParaReintento() throws Exception {
        OutboxEventEntity event = createEvent(
                5L, OutboxStatus.PUBLICANDO, 1, NOW
        );

        when(claimer.claimNextBatch()).thenReturn(List.of(event));
        when(client.post(event.getPayload()))
                .thenThrow(new IOException("timeout"));
        when(repository.findById(5L)).thenReturn(Optional.of(event));

        publisher().publishPendingEvents();

        assertEquals(OutboxStatus.ERROR_REINTENTABLE, event.getStatus());
        assertEquals(
                "HTTP failure: IOException: timeout",
                event.getLastError()
        );
        assertEquals(NOW.plusSeconds(5), event.getAvailableAt());

        verify(repository).save(event);
        verify(repository, never()).delete(any(OutboxEventEntity.class));
    }

    @Test
    void error500QuedaReintentable() throws Exception {
        OutboxEventEntity event = createEvent(
                6L, OutboxStatus.PUBLICANDO, 1, NOW
        );

        when(claimer.claimNextBatch()).thenReturn(List.of(event));
        HttpResponse<Void> errorResponse = successResponse(500);
        when(client.post(event.getPayload())).thenReturn(errorResponse);
        when(repository.findById(6L)).thenReturn(Optional.of(event));

        publisher().publishPendingEvents();

        assertEquals(OutboxStatus.ERROR_REINTENTABLE, event.getStatus());
        assertEquals("HTTP 500", event.getLastError());
        assertEquals(NOW.plusSeconds(5), event.getAvailableAt());

        verify(repository).save(event);
    }

    @Test
    void errorHttpNoPierdeElEvento() throws Exception {
        OutboxEventEntity event = createEvent(
                7L, OutboxStatus.PUBLICANDO, 1, NOW
        );

        HttpHeaders headers = HttpHeaders.of(
                Map.of("Retry-After", List.of("30")),
                (name, value) -> true
        );

        when(claimer.claimNextBatch()).thenReturn(List.of(event));
        when(client.post(event.getPayload())).thenReturn(response);
        when(response.statusCode()).thenReturn(429);
        when(response.headers()).thenReturn(headers);
        when(repository.findById(7L)).thenReturn(Optional.of(event));

        publisher().publishPendingEvents();

        assertEquals(OutboxStatus.ERROR_REINTENTABLE, event.getStatus());
        assertEquals("HTTP 429", event.getLastError());
        assertEquals(NOW.plusSeconds(30), event.getAvailableAt());

        verify(repository).save(event);
        verify(repository, never()).delete(any(OutboxEventEntity.class));
    }

    @Test
    void error400QuedaPermanenteEnElProcesador() throws Exception {
        OutboxEventEntity event = createEvent(
                8L, OutboxStatus.PUBLICANDO, 1, NOW
        );

        when(claimer.claimNextBatch()).thenReturn(List.of(event));
        HttpResponse<Void> errorResponse = successResponse(400);
        when(client.post(event.getPayload())).thenReturn(errorResponse);
        when(repository.findById(8L)).thenReturn(Optional.of(event));

        publisher().publishPendingEvents();

        assertEquals(OutboxStatus.ERROR_PERMANENTE, event.getStatus());
        assertEquals("HTTP 400", event.getLastError());
        assertEquals(null, event.getPublishingSince());

        verify(repository).save(event);
    }

    private OutboxHttpPublisher publisher() {
        return new OutboxHttpPublisher(
                claimer, client, results, clock
        );
    }

    private HttpResponse<Void> successResponse(int status) {
        HttpResponse<Void> responseMock = mock(HttpResponse.class);

        when(responseMock.statusCode()).thenReturn(status);

        lenient().when(responseMock.headers()).thenReturn(
                HttpHeaders.of(Map.of(), (name, value) -> true)
        );

        return responseMock;
    }

    private OutboxEventEntity createEvent(
            Long id,
            OutboxStatus status,
            int attempts,
            Instant publishingSince
    ) throws Exception {
        Constructor<OutboxEventEntity> constructor =
                OutboxEventEntity.class.getDeclaredConstructor();

        constructor.setAccessible(true);

        OutboxEventEntity event = constructor.newInstance();

        Field idField = OutboxEventEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(event, id);

        event.setEventType("PrestamoDevuelto");
        event.setEventVersion(1);
        event.setAggregateId(150L);
        event.setPayload("{\"prestamoId\":150}");
        event.setStatus(status);
        event.setAttempts(attempts);
        event.setCreatedAt(NOW);
        event.setAvailableAt(NOW);
        event.setPublishingSince(publishingSince);

        return event;
    }
}
