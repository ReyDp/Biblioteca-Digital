/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class BibliotecaDigitalHttpClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    private final TokenProvider tokenProvider;

    public BibliotecaDigitalHttpClient(HttpClient httpClient, String baseUrl, TokenProvider tokenProvider) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.tokenProvider = tokenProvider;
    }

    public HttpResponse<String> get(String path) throws Exception {
        String token = this.tokenProvider.token();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(this.baseUrl + path)).header("Authorization", "Bearer " + token).header("Accept", "application/json").GET().build();
        return this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> post(String path, String body) throws Exception {
        String token = this.tokenProvider.token();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(this.baseUrl + path)).header("Authorization", "Bearer " + token).header("Accept", "application/json").header("Content-Type", "application/json").header("Idempotency-Key", UUID.randomUUID().toString()).POST(body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody()).build();
        return this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static interface TokenProvider {
        public String token();
    }
}

