/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.EntityManagerFactory
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
 *  org.springframework.boot.context.properties.EnableConfigurationProperties
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.scheduling.annotation.EnableScheduling
 */
package com.ingsoft.prestamos.infrastructure.config;

import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.JdkOutboxHttpClient;
import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.OutboxEventClaimer;
import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.OutboxHttpClient;
import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.OutboxHttpPublisher;
import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.OutboxPublicationResultProcessor;
import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.OutboxPublisherProperties;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxEventRepository;
import com.ingsoft.prestamos.infrastructure.security.ServiceJwtTokenProvider;
import jakarta.persistence.EntityManagerFactory;
import java.net.http.HttpClient;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnBean(value={EntityManagerFactory.class})
@EnableConfigurationProperties(value={OutboxPublisherProperties.class})
public class OutboxPublisherConfiguration {
    @Bean
    OutboxEventClaimer outboxEventClaimer(OutboxEventRepository repository, Clock clock, OutboxPublisherProperties properties) {
        return new OutboxEventClaimer(repository, clock, properties);
    }

    @Bean
    OutboxPublicationResultProcessor outboxPublicationResultProcessor(OutboxEventRepository repository, Clock clock, OutboxPublisherProperties properties) {
        return new OutboxPublicationResultProcessor(repository, clock, properties);
    }

    @Configuration
    @EnableScheduling
    @EnableConfigurationProperties(OutboxPublisherProperties.class)
    @ConditionalOnBean(EntityManagerFactory.class)
    @ConditionalOnExpression(value="'${outbox.publisher.url:}'.trim().length() > 0 and '${outbox.publisher.jwt-private-key-location:}'.trim().length() > 0")
    static class EnabledPublisherConfiguration {

        @Bean
        ServiceJwtTokenProvider serviceJwtTokenProvider(OutboxPublisherProperties properties, Clock clock) {
            return new ServiceJwtTokenProvider(properties, clock);
        }

        @Bean
        OutboxHttpClient outboxHttpClient(OutboxPublisherProperties properties, ServiceJwtTokenProvider tokenProvider) {
            return new JdkOutboxHttpClient(HttpClient.newHttpClient(), properties, tokenProvider);
        }

        @Bean
        OutboxHttpPublisher outboxHttpPublisher(OutboxEventClaimer claimer, OutboxHttpClient client, OutboxPublicationResultProcessor results, Clock clock) {
            return new OutboxHttpPublisher(claimer, client, results, clock);
        }
    }
}

