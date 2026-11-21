/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.http;

import com.ingsoft.prestamos.application.port.output.ClienteQueryPort;
import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.infrastructure.adapter.output.http.BibliotecaDigitalHttpClient;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpClienteQueryAdapter
implements ClienteQueryPort {
    private final BibliotecaDigitalHttpClient httpClient;

    public HttpClienteQueryAdapter(BibliotecaDigitalHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean existe(ClienteId clienteId) {
        try {
            HttpResponse<String> response = this.httpClient.get("/api/clientes/" + clienteId.value() + "/estado-prestamo");
            if (response.statusCode() == 200) {
                return this.extractExiste(response.body());
            }
            if (response.statusCode() == 404) {
                return false;
            }
            throw new RuntimeException("Error consultando cliente " + clienteId.value() + ": HTTP " + response.statusCode());
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrumpido consultando cliente.", ex);
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new RuntimeException("Error consultando cliente " + clienteId.value() + ".", ex);
        }
    }

    private boolean extractExiste(String body) {
        Pattern pattern = Pattern.compile("\"existe\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }
        throw new RuntimeException("Respuesta inesperada al consultar cliente: " + body);
    }
}

