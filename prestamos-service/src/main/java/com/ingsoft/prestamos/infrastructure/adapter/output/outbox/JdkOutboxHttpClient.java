/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.outbox;

import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.OutboxHttpClient;
import com.ingsoft.prestamos.infrastructure.adapter.output.outbox.OutboxPublisherProperties;
import com.ingsoft.prestamos.infrastructure.security.ServiceJwtTokenProvider;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class JdkOutboxHttpClient
implements OutboxHttpClient {
    private final HttpClient client;
    private final OutboxPublisherProperties properties;
    private final ServiceJwtTokenProvider tokenProvider;

    public JdkOutboxHttpClient(HttpClient client, OutboxPublisherProperties properties, ServiceJwtTokenProvider tokenProvider) {
        this.client = client;
        this.properties = properties;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public HttpResponse<Void> post(String payload) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(this.properties.getUrl()).timeout(this.properties.getHttpTimeout()).header("Content-Type", "application/json").header("Authorization", "Bearer " + this.tokenProvider.token()).POST(HttpRequest.BodyPublishers.ofString(payload)).build();
        return this.client.send(request, HttpResponse.BodyHandlers.discarding());
    }
}

