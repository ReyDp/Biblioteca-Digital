
package com.ingsoft.prestamos.infrastructure.adapter.output.http;

import com.ingsoft.prestamos.domain.model.EstadoReserva;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import com.ingsoft.prestamos.domain.model.ReservaLibro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpReservaLibroAdapterTest {

    @Mock
    private BibliotecaDigitalHttpClient httpClient;

    @Mock
    private HttpResponse<String> response;

    private HttpReservaLibroAdapter adapter;

    private final LibroId libroId = new LibroId(10L);
    private final ReservaId reservaId = new ReservaId("reserva-001");

    private final String jsonReserva =
            """
            {
                "reservaId": "reserva-001",
                "libroId": 10,
                "estado": "VIGENTE"
            }
            """;

    @BeforeEach
    void setUp() {
        adapter = new HttpReservaLibroAdapter(httpClient);
    }

    @Test
    void solicitarReserva_exitosa() throws Exception {
        when(httpClient.post(
                "/api/libros/10/reservas-prestamo", null
        )).thenReturn(response);

        when(response.statusCode()).thenReturn(201);
        when(response.body()).thenReturn(jsonReserva);

        ReservaLibro resultado = adapter.solicitarReserva(libroId);

        assertNotNull(resultado);
        assertEquals(reservaId, resultado.reservaId());
        assertEquals(libroId, resultado.libroId());
        assertEquals(EstadoReserva.VIGENTE, resultado.estado());

        verify(httpClient).post(
                "/api/libros/10/reservas-prestamo", null
        );
    }

    @Test
    void solicitarReserva_libroNoExiste() throws Exception {
        when(httpClient.post(
                "/api/libros/10/reservas-prestamo", null
        )).thenReturn(response);

        when(response.statusCode()).thenReturn(404);

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> adapter.solicitarReserva(libroId)
        );

        assertTrue(error.getMessage().contains("no existe"));

        verify(httpClient).post(
                "/api/libros/10/reservas-prestamo", null
        );
    }

    @Test
    void solicitarReserva_libroNoDisponible() throws Exception {
        when(httpClient.post(
                "/api/libros/10/reservas-prestamo", null
        )).thenReturn(response);

        when(response.statusCode()).thenReturn(409);

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> adapter.solicitarReserva(libroId)
        );

        assertTrue(error.getMessage().contains("no está disponible"));

        verify(httpClient).post(
                "/api/libros/10/reservas-prestamo", null
        );
    }

    @Test
    void consultarReserva_existente() throws Exception {
        when(httpClient.get(
                "/api/libros/reservas-prestamo/reserva-001"
        )).thenReturn(response);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(jsonReserva);

        Optional<ReservaLibro> resultado =
                adapter.consultarReserva(reservaId);

        assertTrue(resultado.isPresent());
        assertEquals(reservaId, resultado.get().reservaId());
        assertEquals(libroId, resultado.get().libroId());
        assertEquals(EstadoReserva.VIGENTE, resultado.get().estado());

        verify(httpClient).get(
                "/api/libros/reservas-prestamo/reserva-001"
        );
    }

    @Test
    void consultarReserva_noExistente() throws Exception {
        when(httpClient.get(
                "/api/libros/reservas-prestamo/reserva-001"
        )).thenReturn(response);

        when(response.statusCode()).thenReturn(404);

        Optional<ReservaLibro> resultado =
                adapter.consultarReserva(reservaId);

        assertTrue(resultado.isEmpty());

        verify(httpClient).get(
                "/api/libros/reservas-prestamo/reserva-001"
        );

        verify(response, never()).body();
    }

    @Test
    void confirmarReserva_exitosa() throws Exception {
        String jsonConfirmada =
                """
                {
                    "reservaId": "reserva-001",
                    "libroId": 10,
                    "estado": "CONFIRMADA"
                }
                """;

        when(httpClient.post(
                "/api/libros/reservas-prestamo/reserva-001/confirmar",
                null
        )).thenReturn(response);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(jsonConfirmada);

        ReservaLibro resultado =
                adapter.confirmarReserva(reservaId);

        assertEquals(reservaId, resultado.reservaId());
        assertEquals(libroId, resultado.libroId());
        assertEquals(EstadoReserva.CONFIRMADA, resultado.estado());
        assertTrue(resultado.estaConfirmada());

        verify(httpClient).post(
                "/api/libros/reservas-prestamo/reserva-001/confirmar",
                null
        );
    }

    @Test
    void confirmarReserva_noExiste() throws Exception {
        when(httpClient.post(
                "/api/libros/reservas-prestamo/reserva-001/confirmar",
                null
        )).thenReturn(response);

        when(response.statusCode()).thenReturn(404);

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> adapter.confirmarReserva(reservaId)
        );

        assertTrue(error.getMessage().contains("no existe"));

        verify(httpClient).post(
                "/api/libros/reservas-prestamo/reserva-001/confirmar",
                null
        );
    }

    @Test
    void cancelarReserva_exitosa() throws Exception {
        String jsonCancelada =
                """
                {
                    "reservaId": "reserva-001",
                    "libroId": 10,
                    "estado": "CANCELADA"
                }
                """;

        when(httpClient.post(
                "/api/libros/reservas-prestamo/reserva-001/cancelar",
                null
        )).thenReturn(response);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(jsonCancelada);

        ReservaLibro resultado =
                adapter.cancelarReserva(reservaId);

        assertEquals(reservaId, resultado.reservaId());
        assertEquals(libroId, resultado.libroId());
        assertEquals(EstadoReserva.CANCELADA, resultado.estado());

        verify(httpClient).post(
                "/api/libros/reservas-prestamo/reserva-001/cancelar",
                null
        );
    }
}
