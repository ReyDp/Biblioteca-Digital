/*
 * Decompiled with CFR 0.152.
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.http;

import com.ingsoft.prestamos.application.port.output.ReservaLibroPort;
import com.ingsoft.prestamos.domain.model.EstadoReserva;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import com.ingsoft.prestamos.domain.model.ReservaLibro;
import com.ingsoft.prestamos.infrastructure.adapter.output.http.BibliotecaDigitalHttpClient;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpReservaLibroAdapter
implements ReservaLibroPort {
    private final BibliotecaDigitalHttpClient httpClient;

    public HttpReservaLibroAdapter(BibliotecaDigitalHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public ReservaLibro solicitarReserva(LibroId libroId) {
        try {
            HttpResponse<String> response = this.httpClient.post("/api/libros/" + libroId.value() + "/reservas-prestamo", null);
            if (response.statusCode() == 201) {
                return this.parseReserva(response.body());
            }
            if (response.statusCode() == 404) {
                throw new RuntimeException("El libro " + libroId.value() + " no existe.");
            }
            if (response.statusCode() == 409) {
                throw new RuntimeException("El libro " + libroId.value() + " no est\u00e1 disponible para reserva.");
            }
            throw new RuntimeException("Error reservando libro " + libroId.value() + ": HTTP " + response.statusCode());
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrumpido reservando libro.", ex);
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new RuntimeException("Error reservando libro " + libroId.value() + ".", ex);
        }
    }

    @Override
    public Optional<ReservaLibro> consultarReserva(ReservaId reservaId) {
        try {
            HttpResponse<String> response = this.httpClient.get("/api/libros/reservas-prestamo/" + reservaId.value());
            if (response.statusCode() == 200) {
                return Optional.of(this.parseReserva(response.body()));
            }
            if (response.statusCode() == 404) {
                return Optional.empty();
            }
            throw new RuntimeException("Error consultando reserva " + reservaId.value() + ": HTTP " + response.statusCode());
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrumpido consultando reserva.", ex);
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new RuntimeException("Error consultando reserva " + reservaId.value() + ".", ex);
        }
    }

    @Override
    public ReservaLibro confirmarReserva(ReservaId reservaId) {
        try {
            HttpResponse<String> response = this.httpClient.post("/api/libros/reservas-prestamo/" + reservaId.value() + "/confirmar", null);
            if (response.statusCode() == 200) {
                return this.parseReserva(response.body());
            }
            if (response.statusCode() == 404) {
                throw new RuntimeException("La reserva " + reservaId.value() + " no existe.");
            }
            if (response.statusCode() == 409) {
                throw new RuntimeException("No se puede confirmar la reserva " + reservaId.value() + ".");
            }
            throw new RuntimeException("Error confirmando reserva " + reservaId.value() + ": HTTP " + response.statusCode());
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrumpido confirmando reserva.", ex);
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new RuntimeException("Error confirmando reserva " + reservaId.value() + ".", ex);
        }
    }

    @Override
    public ReservaLibro cancelarReserva(ReservaId reservaId) {
        try {
            HttpResponse<String> response = this.httpClient.post("/api/libros/reservas-prestamo/" + reservaId.value() + "/cancelar", null);
            if (response.statusCode() == 200) {
                return this.parseReserva(response.body());
            }
            if (response.statusCode() == 404) {
                throw new RuntimeException("La reserva " + reservaId.value() + " no existe.");
            }
            if (response.statusCode() == 409) {
                throw new RuntimeException("No se puede cancelar la reserva " + reservaId.value() + ".");
            }
            throw new RuntimeException("Error cancelando reserva " + reservaId.value() + ": HTTP " + response.statusCode());
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrumpido cancelando reserva.", ex);
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new RuntimeException("Error cancelando reserva " + reservaId.value() + ".", ex);
        }
    }

    private ReservaLibro parseReserva(String body) {
        String reservaId = this.extractString(body, "\"reservaId\"\\s*:\\s*\"([^\"]+)\"");
        Long libroId = this.extractLong(body, "\"libroId\"\\s*:\\s*(\\d+)");
        String estado = this.extractString(body, "\"estado\"\\s*:\\s*\"([^\"]+)\"");
        return new ReservaLibro(new ReservaId(reservaId), new LibroId(libroId), EstadoReserva.valueOf(estado));
    }

    private String extractString(String body, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("No se pudo parsear la respuesta: " + body);
    }

    private Long extractLong(String body, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        throw new RuntimeException("No se pudo parsear la respuesta: " + body);
    }
}

