
package com.ingsoft.prestamos.application.service;

import com.ingsoft.prestamos.application.port.output.ClienteQueryPort;
import com.ingsoft.prestamos.application.port.output.PrestamoPersistencePort;
import com.ingsoft.prestamos.application.port.output.ReservaLibroPort;
import com.ingsoft.prestamos.domain.exception.ClienteNoExisteException;
import com.ingsoft.prestamos.domain.exception.LibroNoDisponibleException;
import com.ingsoft.prestamos.domain.exception.ReservaNoConfirmadaException;
import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.EstadoPrestamo;
import com.ingsoft.prestamos.domain.model.EstadoReserva;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.ReservaId;
import com.ingsoft.prestamos.domain.model.ReservaLibro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarPrestamoServiceTest {

    @Mock
    private PrestamoPersistencePort prestamoPersistence;

    @Mock
    private ClienteQueryPort clienteQuery;

    @Mock
    private ReservaLibroPort reservaLibro;

    private RegistrarPrestamoService service;

    private final ClienteId clienteId = new ClienteId(1L);
    private final LibroId libroId = new LibroId(10L);
    private final ReservaId reservaId = new ReservaId("reserva-001");

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-11-19T12:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {
        service = new RegistrarPrestamoService(
                prestamoPersistence,
                clienteQuery,
                reservaLibro,
                clock
        );
    }

    private void prepararClienteYLibroDisponibles() {
        when(clienteQuery.existe(clienteId)).thenReturn(true);

        when(prestamoPersistence.existePrestamoQueOcupaLibro(libroId))
                .thenReturn(false);
    }

    private void configurarGuardadoSinAlterarPrestamo() {
        when(prestamoPersistence.guardar(any(Prestamo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void rechazaClienteInexistenteSinReservar() {
        when(clienteQuery.existe(clienteId)).thenReturn(false);

        assertThrows(
                ClienteNoExisteException.class,
                () -> service.registrar(clienteId, libroId)
        );

        verifyNoInteractions(reservaLibro);
        verify(prestamoPersistence, never()).guardar(any());
    }

    @Test
    void rechazaLibroConPrestamoOcupante() {
        when(clienteQuery.existe(clienteId))
                .thenReturn(true);
        when(prestamoPersistence.existePrestamoQueOcupaLibro(libroId))
                .thenReturn(true);

        assertThrows(
                LibroNoDisponibleException.class,
                () -> service.registrar(clienteId, libroId)
        );

        verifyNoInteractions(reservaLibro);
        verify(prestamoPersistence, never()).guardar(any());
    }

    @Test
    void rechazaLibroNoDisponible() {
        prepararClienteYLibroDisponibles();
        ReservaLibro reservaNoDisponible = new ReservaLibro(
                reservaId,
                libroId,
                EstadoReserva.CANCELADA
        );

        when(reservaLibro.solicitarReserva(libroId))
                .thenReturn(reservaNoDisponible);

        assertThrows(
                LibroNoDisponibleException.class,
                () -> service.registrar(clienteId, libroId)
        );

        verify(prestamoPersistence, never()).guardar(any());
        verify(reservaLibro, never()).confirmarReserva(any());
    }

    @Test
    void falloDePersistenciaDelPendienteSePropagaSinCancelarReserva() {
        prepararClienteYLibroDisponibles();
        ReservaLibro reservaVigente = new ReservaLibro(
                reservaId,
                libroId,
                EstadoReserva.VIGENTE
        );

        RuntimeException error = new RuntimeException("Error de persistencia");

        when(reservaLibro.solicitarReserva(libroId))
                .thenReturn(reservaVigente);

        when(prestamoPersistence.guardar(any(Prestamo.class)))
                .thenThrow(error);

        RuntimeException resultado = assertThrows(
                RuntimeException.class,
                () -> service.registrar(clienteId, libroId)
        );

        assertSame(error, resultado);

        verify(reservaLibro, never()).confirmarReserva(any());
        verify(reservaLibro, never()).cancelarReserva(any());
    }

    @Test
    void timeoutDeConfirmacionNoCancelaNiOcultaError() {
        prepararClienteYLibroDisponibles();
        configurarGuardadoSinAlterarPrestamo();
        ReservaLibro reservaVigente = new ReservaLibro(
                reservaId,
                libroId,
                EstadoReserva.VIGENTE
        );

        RuntimeException timeout = new RuntimeException("Timeout de confirmación");

        when(reservaLibro.solicitarReserva(libroId))
                .thenReturn(reservaVigente);

        when(reservaLibro.confirmarReserva(reservaId))
                .thenThrow(timeout);

        RuntimeException resultado = assertThrows(
                RuntimeException.class,
                () -> service.registrar(clienteId, libroId)
        );

        assertSame(timeout, resultado);

        verify(prestamoPersistence, times(1)).guardar(any(Prestamo.class));
        verify(reservaLibro, never()).cancelarReserva(any());
    }

    @Test
    void confirmacionNoExitosaMarcaCompensacion() {
        prepararClienteYLibroDisponibles();
        configurarGuardadoSinAlterarPrestamo();
        ReservaLibro reservaVigente = new ReservaLibro(
                reservaId,
                libroId,
                EstadoReserva.VIGENTE
        );

        ReservaLibro reservaSinConfirmar = new ReservaLibro(
                reservaId,
                libroId,
                EstadoReserva.CONFIRMANDO
        );

        when(reservaLibro.solicitarReserva(libroId))
                .thenReturn(reservaVigente);

        when(reservaLibro.confirmarReserva(reservaId))
                .thenReturn(reservaSinConfirmar);

        assertThrows(
                ReservaNoConfirmadaException.class,
                () -> service.registrar(clienteId, libroId)
        );

        ArgumentCaptor<Prestamo> captor =
                ArgumentCaptor.forClass(Prestamo.class);

        verify(prestamoPersistence, times(2)).guardar(captor.capture());

        Prestamo prestamoGuardadoFinal = captor.getAllValues().get(1);

        assertEquals(
                EstadoPrestamo.COMPENSACION_REQUERIDA,
                prestamoGuardadoFinal.estado()
        );

        verify(reservaLibro, never()).cancelarReserva(any());
    }

    @Test
    void registraPendienteConfirmaYActiva() {
        prepararClienteYLibroDisponibles();
        configurarGuardadoSinAlterarPrestamo();
        ReservaLibro reservaVigente = new ReservaLibro(
                reservaId,
                libroId,
                EstadoReserva.VIGENTE
        );

        ReservaLibro reservaConfirmada = new ReservaLibro(
                reservaId,
                libroId,
                EstadoReserva.CONFIRMADA
        );

        when(reservaLibro.solicitarReserva(libroId))
                .thenReturn(reservaVigente);

        when(reservaLibro.confirmarReserva(reservaId))
                .thenReturn(reservaConfirmada);

        Prestamo resultado = service.registrar(clienteId, libroId);

        assertNotNull(resultado);
        assertTrue(resultado.esActivo());

        assertEquals(
                LocalDate.now(clock).plusDays(15),
                resultado.fechaLimite()
        );

        verify(prestamoPersistence, times(2)).guardar(any(Prestamo.class));
        verify(reservaLibro).solicitarReserva(libroId);
        verify(reservaLibro).confirmarReserva(reservaId);
        verify(reservaLibro, never()).cancelarReserva(any());
    }
}

