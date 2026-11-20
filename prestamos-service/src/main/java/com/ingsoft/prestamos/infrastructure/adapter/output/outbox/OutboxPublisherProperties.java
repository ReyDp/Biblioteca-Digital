/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.outbox;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value="outbox.publisher")
public class OutboxPublisherProperties {
    private URI url;
    private Duration pollInterval = Duration.ofSeconds(10L);
    private int batchSize = 20;
    private Duration httpTimeout = Duration.ofSeconds(5L);
    private int maxAttempts = 8;
    private Duration initialBackoff = Duration.ofSeconds(5L);
    private Duration maxBackoff = Duration.ofMinutes(5L);
    private Duration leaseDuration = Duration.ofMinutes(1L);
    private String jwtPrivateKeyLocation;
    private String jwtIssuer = "prestamos-service";
    private String jwtAudience = "biblioteca-digital";
    private Duration jwtLifetime = Duration.ofMinutes(5L);

    public URI getUrl() {
        return this.url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public Duration getPollInterval() {
        return this.pollInterval;
    }

    public void setPollInterval(Duration value) {
        this.pollInterval = value;
    }

    public int getBatchSize() {
        return this.batchSize;
    }

    public void setBatchSize(int value) {
        this.batchSize = value;
    }

    public Duration getHttpTimeout() {
        return this.httpTimeout;
    }

    public void setHttpTimeout(Duration value) {
        this.httpTimeout = value;
    }

    public int getMaxAttempts() {
        return this.maxAttempts;
    }

    public void setMaxAttempts(int value) {
        this.maxAttempts = value;
    }

    public Duration getInitialBackoff() {
        return this.initialBackoff;
    }

    public void setInitialBackoff(Duration value) {
        this.initialBackoff = value;
    }

    public Duration getMaxBackoff() {
        return this.maxBackoff;
    }

    public void setMaxBackoff(Duration value) {
        this.maxBackoff = value;
    }

    public Duration getLeaseDuration() {
        return this.leaseDuration;
    }

    public void setLeaseDuration(Duration value) {
        this.leaseDuration = value;
    }

    public String getJwtPrivateKeyLocation() {
        return this.jwtPrivateKeyLocation;
    }

    public void setJwtPrivateKeyLocation(String value) {
        this.jwtPrivateKeyLocation = value;
    }

    public String getJwtIssuer() {
        return this.jwtIssuer;
    }

    public void setJwtIssuer(String value) {
        this.jwtIssuer = value;
    }

    public String getJwtAudience() {
        return this.jwtAudience;
    }

    public void setJwtAudience(String value) {
        this.jwtAudience = value;
    }

    public Duration getJwtLifetime() {
        return this.jwtLifetime;
    }

    public void setJwtLifetime(Duration value) {
        this.jwtLifetime = value;
    }
}

