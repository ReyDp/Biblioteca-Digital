
package com.ingsoft.prestamos.infrastructure.adapter.output.http;

import com.ingsoft.prestamos.domain.model.ClienteId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpClienteQueryAdapterTest {

    @Mock
    private BibliotecaDigitalHttpClient httpClient;

    @Mock
    private HttpResponse<String> response;

    private HttpClienteQueryAdapter adapter;

    private final ClienteId clienteId = new ClienteId(1L);

    @BeforeEach
    void setUp() {
        adapter = new HttpClienteQueryAdapter(httpClient);
    }

    @Test
    void clienteNoExiste_retornaFalse() throws Exception {
        when(httpClient.get("/api/clientes/1/estado-prestamo"))
                .thenReturn(response);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"existe\": false}");

        boolean resultado = adapter.existe(clienteId);

        assertFalse(resultado);

        verify(httpClient).get("/api/clientes/1/estado-prestamo");
    }

    @Test
    void clienteExiste_retornaTrue() throws Exception {
        when(httpClient.get("/api/clientes/1/estado-prestamo"))
                .thenReturn(response);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"existe\": true}");

        boolean resultado = adapter.existe(clienteId);

        assertTrue(resultado);

        verify(httpClient).get("/api/clientes/1/estado-prestamo");
    }

    @Test
    void errorHttp_propagaExcepcion() throws Exception {
        when(httpClient.get("/api/clientes/1/estado-prestamo"))
                .thenReturn(response);

        when(response.statusCode()).thenReturn(500);

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> adapter.existe(clienteId)
        );

        assertTrue(error.getMessage().contains("HTTP 500"));

        verify(httpClient).get("/api/clientes/1/estado-prestamo");
    }

    @Test
    void cliente404_retornaFalse() throws Exception {
        when(httpClient.get("/api/clientes/1/estado-prestamo"))
                .thenReturn(response);

        when(response.statusCode()).thenReturn(404);

        boolean resultado = adapter.existe(clienteId);

        assertFalse(resultado);

        verify(httpClient).get("/api/clientes/1/estado-prestamo");

        verify(response, never()).body();
    }
}
