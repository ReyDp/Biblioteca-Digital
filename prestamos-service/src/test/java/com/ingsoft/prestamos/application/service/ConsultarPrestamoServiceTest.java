
package com.ingsoft.prestamos.application.service;

import com.ingsoft.prestamos.application.port.output.PrestamoPersistencePort;
import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.EstadoPrestamo;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarPrestamoServiceTest {

    @Mock
    private PrestamoPersistencePort prestamoPersistence;

    private ConsultarPrestamoService service;

    private final PrestamoId prestamoId = new PrestamoId(100L);
    private final ClienteId clienteId = new ClienteId(1L);

    @BeforeEach
    void setUp() {
        service = new ConsultarPrestamoService(prestamoPersistence);
    }

    private Prestamo crearPrestamo() {
        LocalDate fechaPrestamo = LocalDate.of(2026, 11, 1);

        return Prestamo.rehidratar(
                prestamoId,
                clienteId,
                new LibroId(10L),
                new ReservaId("reserva-001"),
                fechaPrestamo,
                fechaPrestamo.plusDays(15),
                null,
                EstadoPrestamo.ACTIVO
        );
    }

    @Test
    void delegaConsultasAlPuertoDePersistencia() {
        Prestamo prestamo = crearPrestamo();

        when(prestamoPersistence.buscarPorId(prestamoId))
                .thenReturn(Optional.of(prestamo));

        when(prestamoPersistence.buscarPorCliente(clienteId))
                .thenReturn(List.of(prestamo));

        Optional<Prestamo> resultadoPorId =
                service.buscarPorId(prestamoId);

        List<Prestamo> resultadoPorCliente =
                service.buscarPorCliente(clienteId);

        assertTrue(resultadoPorId.isPresent());
        assertSame(prestamo, resultadoPorId.get());

        assertEquals(1, resultadoPorCliente.size());
        assertSame(prestamo, resultadoPorCliente.get(0));

        verify(prestamoPersistence).buscarPorId(prestamoId);
        verify(prestamoPersistence).buscarPorCliente(clienteId);
    }

    @Test
    void buscarPorIdRetornaOptionalVacioCuandoNoExiste() {
        when(prestamoPersistence.buscarPorId(prestamoId))
                .thenReturn(Optional.empty());

        Optional<Prestamo> resultado =
                service.buscarPorId(prestamoId);

        assertTrue(resultado.isEmpty());

        verify(prestamoPersistence).buscarPorId(prestamoId);
    }

    @Test
    void buscarPorClienteRetornaListaInmodificable() {
        Prestamo prestamo = crearPrestamo();

        when(prestamoPersistence.buscarPorCliente(clienteId))
                .thenReturn(List.of(prestamo));

        List<Prestamo> resultado =
                service.buscarPorCliente(clienteId);

        assertThrows(
                UnsupportedOperationException.class,
                () -> resultado.add(crearPrestamo())
        );

        verify(prestamoPersistence).buscarPorCliente(clienteId);
    }
}
