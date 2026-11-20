/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.outbox;

import java.io.IOException;
import java.net.http.HttpResponse;

public interface OutboxHttpClient {
    public HttpResponse<Void> post(String var1) throws IOException, InterruptedException;
}

