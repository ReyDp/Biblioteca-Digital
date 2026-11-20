
package com.ingsoft.prestamos.application.service;

import com.ingsoft.prestamos.application.port.output.PrestamoPersistencePort;
import com.ingsoft.prestamos.application.port.output.PublicarPrestamoDevueltoPort;
import com.ingsoft.prestamos.domain.exception.ConfirmacionPendienteException;
import com.ingsoft.prestamos.domain.exception.PrestamoNoExisteException;
import com.ingsoft.prestamos.domain.exception.PrestamoYaDevueltoException;
import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.EstadoPrestamo;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarDevolucionServiceTest {

    @Mock
    private PrestamoPersistencePort prestamoPersistence;

    @Mock
    private PublicarPrestamoDevueltoPort publicador;

    private RegistrarDevolucionService service;

    private final PrestamoId prestamoId = new PrestamoId(100L);

    private final ClienteId clienteId = new ClienteId(1L);

    private final LibroId libroId = new LibroId(10L);

    private final ReservaId reservaId = new ReservaId("reserva-001");

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-11-19T12:00:00Z"),
            ZoneOffset.UTC
    );

    private final LocalDate fechaPrestamo = LocalDate.of(2026, 11, 1);

    private final LocalDate fechaActual = LocalDate.of(2026, 11, 19);

    @BeforeEach
    void setUp() {
        service = new RegistrarDevolucionService(
                prestamoPersistence,
                publicador,
                clock
        );
    }

    private Prestamo crearPrestamoActivo() {
        Prestamo prestamo = Prestamo.rehidratar(
                prestamoId,
                clienteId,
                libroId,
                reservaId,
                fechaPrestamo,
                fechaPrestamo.plusDays(15),
                null,
                EstadoPrestamo.ACTIVO
        );

        return prestamo;
    }

    @Test
    void devuelveGuardaYPublicaEnOrden() {
        Prestamo prestamo = crearPrestamoActivo();

        when(prestamoPersistence.buscarPorIdConBloqueo(prestamoId))
                .thenReturn(Optional.of(prestamo));

        when(prestamoPersistence.guardar(prestamo))
                .thenReturn(prestamo);

        Prestamo resultado = service.registrar(prestamoId);

        assertNotNull(resultado);
        assertEquals(EstadoPrestamo.DEVUELTO, resultado.estado());
        assertEquals(fechaActual, resultado.fechaDevolucion());

        InOrder orden = inOrder(prestamoPersistence, publicador);

        orden.verify(prestamoPersistence).buscarPorIdConBloqueo(prestamoId);
        orden.verify(prestamoPersistence).guardar(prestamo);
        orden.verify(publicador).publicar(prestamo);

        verifyNoMoreInteractions(publicador);
    }

    @Test
    void yaDevueltoSeRechaza() {
        Prestamo prestamoDevuelto = Prestamo.rehidratar(
                prestamoId,
                clienteId,
                libroId,
                reservaId,
                fechaPrestamo,
                fechaPrestamo.plusDays(15),
                fechaActual.minusDays(1),
                EstadoPrestamo.DEVUELTO
        );

        when(prestamoPersistence.buscarPorIdConBloqueo(prestamoId))
                .thenReturn(Optional.of(prestamoDevuelto));

        assertThrows(
                PrestamoYaDevueltoException.class,
                () -> service.registrar(prestamoId)
        );

        verify(prestamoPersistence, never()).guardar(any());
        verifyNoInteractions(publicador);
    }

    @Test
    void pendienteSeRechaza() {
        Prestamo prestamoPendiente = Prestamo.crearPendiente(
                clienteId,
                libroId,
                reservaId,
                fechaPrestamo
        ).conId(prestamoId);

        when(prestamoPersistence.buscarPorIdConBloqueo(prestamoId))
                .thenReturn(Optional.of(prestamoPendiente));

        assertThrows(
                ConfirmacionPendienteException.class,
                () -> service.registrar(prestamoId)
        );

        verify(prestamoPersistence, never()).guardar(any());
        verifyNoInteractions(publicador);
    }

    @Test
    void inexistenteNoGuardaNiPublica() {
        when(prestamoPersistence.buscarPorIdConBloqueo(prestamoId))
                .thenReturn(Optional.empty());

        assertThrows(
                PrestamoNoExisteException.class,
                () -> service.registrar(prestamoId)
        );

        verify(prestamoPersistence, never()).guardar(any());
        verifyNoInteractions(publicador);
    }

    @Test
    void falloDePersistenciaSePropagaSinPublicar() {
        Prestamo prestamo = crearPrestamoActivo();

        RuntimeException error = new RuntimeException(
                "Error de persistencia"
        );

        when(prestamoPersistence.buscarPorIdConBloqueo(prestamoId))
                .thenReturn(Optional.of(prestamo));

        when(prestamoPersistence.guardar(prestamo))
                .thenThrow(error);

        RuntimeException resultado = assertThrows(
                RuntimeException.class,
                () -> service.registrar(prestamoId)
        );

        assertSame(error, resultado);

        verify(prestamoPersistence).guardar(prestamo);
        verifyNoInteractions(publicador);
    }

    @Test
    void falloDePublicacionSePropaga() {
        Prestamo prestamo = crearPrestamoActivo();

        RuntimeException error = new RuntimeException(
                "Error al publicar evento"
        );

        when(prestamoPersistence.buscarPorIdConBloqueo(prestamoId))
                .thenReturn(Optional.of(prestamo));

        when(prestamoPersistence.guardar(prestamo))
                .thenReturn(prestamo);

        doThrow(error)
                .when(publicador)
                .publicar(prestamo);

        RuntimeException resultado = assertThrows(
                RuntimeException.class,
                () -> service.registrar(prestamoId)
        );

        assertSame(error, resultado);
        assertEquals(EstadoPrestamo.DEVUELTO, prestamo.estado());
        assertEquals(fechaActual, prestamo.fechaDevolucion());

        verify(prestamoPersistence).guardar(prestamo);
        verify(publicador).publicar(prestamo);
    }
}
