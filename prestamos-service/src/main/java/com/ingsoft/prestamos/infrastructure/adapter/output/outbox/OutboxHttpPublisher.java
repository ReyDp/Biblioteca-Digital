/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.scheduling.annotation.Scheduled
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.outbox;

import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.OutboxEventClaimer;
import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.OutboxHttpClient;
import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.OutboxPublicationResultProcessor;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxEventEntity;
import java.io.IOException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.scheduling.annotation.Scheduled;

public class OutboxHttpPublisher {
    private final OutboxEventClaimer claimer;
    private final OutboxHttpClient client;
    private final OutboxPublicationResultProcessor results;
    private final Clock clock;

    public OutboxHttpPublisher(OutboxEventClaimer claimer, OutboxHttpClient client, OutboxPublicationResultProcessor results, Clock clock) {
        this.claimer = claimer;
        this.client = client;
        this.results = results;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString="${outbox.publisher.poll-interval:PT10S}")
    public void publishPendingEvents() {
        this.claimer.claimNextBatch().forEach(this::publish);
    }

    private void publish(OutboxEventEntity event) {
        try {
            HttpResponse<Void> response = this.client.post(event.getPayload());
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                this.results.published(event);
            } else if (status == 408 || status == 429 || status >= 500) {
                this.results.failed(event, "HTTP " + status, this.retryAfter(response.headers()));
            } else {
                this.results.permanentlyFailed(event, "HTTP " + status);
            }
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            this.results.failed(event, "HTTP interrupted: " + exception.getMessage(), null);
        }
        catch (IOException | RuntimeException exception) {
            this.results.failed(event, "HTTP failure: " + exception.getClass().getSimpleName() + ": " + exception.getMessage(), null);
        }
    }

    private Duration retryAfter(HttpHeaders headers) {
        return headers.firstValue("Retry-After").flatMap(value -> {
            try {
                return Optional.of(Duration.ofSeconds(Long.parseLong(value)));
            }
            catch (NumberFormatException ignored) {
                try {
                    Duration delay = Duration.between(Instant.now(this.clock), ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant());
                    return Optional.of(delay.isNegative() ? Duration.ZERO : delay);
                }
                catch (RuntimeException invalidDate) {
                    return Optional.empty();
                }
            }
        }).orElse(null);
    }
}

