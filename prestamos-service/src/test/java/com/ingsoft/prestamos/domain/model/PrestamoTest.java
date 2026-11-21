
package com.ingsoft.prestamos.domain.model;

import com.ingsoft.prestamos.domain.exception.ConfirmacionPendienteException;
import com.ingsoft.prestamos.domain.exception.PrestamoEstadoNoPermitidoException;
import com.ingsoft.prestamos.domain.exception.PrestamoYaDevueltoException;
import com.ingsoft.prestamos.domain.exception.ReglaPrestamoInvalidaException;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrestamoTest {

    private static final ClienteId CLIENTE_ID = new ClienteId(10L);
    private static final LibroId LIBRO_ID = new LibroId(20L);
    private static final ReservaId RESERVA_ID = new ReservaId("RES-001");
    private static final LocalDate FECHA_PRESTAMO =
            LocalDate.of(2026, 11, 18);

    private Prestamo crearPrestamoPendiente() {
        return Prestamo.crearPendiente(
                CLIENTE_ID,
                LIBRO_ID,
                RESERVA_ID,
                FECHA_PRESTAMO
        );
    }

    private Prestamo crearPrestamoActivo() {
        Prestamo prestamo = crearPrestamoPendiente();
        prestamo.confirmarReserva();
        return prestamo;
    }

    @Test
    void rechazaFechasInvalidas() {
        assertThrows(
                ReglaPrestamoInvalidaException.class,
                () -> Prestamo.rehidratar(
                        new PrestamoId(1L),
                        CLIENTE_ID,
                        LIBRO_ID,
                        RESERVA_ID,
                        FECHA_PRESTAMO,
                        FECHA_PRESTAMO.minusDays(1),
                        null,
                        EstadoPrestamo.ACTIVO
                )
        );

        assertThrows(
                ReglaPrestamoInvalidaException.class,
                () -> Prestamo.rehidratar(
                        new PrestamoId(1L),
                        CLIENTE_ID,
                        LIBRO_ID,
                        RESERVA_ID,
                        FECHA_PRESTAMO,
                        FECHA_PRESTAMO.plusDays(15),
                        FECHA_PRESTAMO.minusDays(1),
                        EstadoPrestamo.DEVUELTO
                )
        );
    }

    @Test
    void noPermiteReactivarUnPrestamoDevuelto() {
        Prestamo prestamo = Prestamo.rehidratar(
                new PrestamoId(1L),
                CLIENTE_ID,
                LIBRO_ID,
                RESERVA_ID,
                FECHA_PRESTAMO,
                FECHA_PRESTAMO.plusDays(15),
                FECHA_PRESTAMO.plusDays(5),
                EstadoPrestamo.DEVUELTO
        );

        assertThrows(
                PrestamoEstadoNoPermitidoException.class,
                prestamo::confirmarReserva
        );
    }

    @Test
    void conservaIdentificadorNumericoAsignadoPorPersistencia() {
        Prestamo prestamo = crearPrestamoPendiente()
                .conId(PrestamoId.asignado(123L));

        assertThat(prestamo.id())
                .contains(new PrestamoId(123L));
    }

    @Test
    void activaDespuesDeConfirmarLaReserva() {
        Prestamo prestamo = crearPrestamoPendiente();

        prestamo.confirmarReserva();

        assertThat(prestamo.estado())
                .isEqualTo(EstadoPrestamo.ACTIVO);

        assertThat(prestamo.esActivo()).isTrue();
    }

    @Test
    void noPermiteDevolverUnPrestamoPendiente() {
        Prestamo prestamo = crearPrestamoPendiente();

        assertThrows(
                ConfirmacionPendienteException.class,
                () -> prestamo.registrarDevolucion(
                        FECHA_PRESTAMO.plusDays(3)
                )
        );
    }

    @Test
    void marcaCompensacionSinLiberarLaReservaDesdeElDominio() {
        Prestamo prestamo = crearPrestamoPendiente();

        prestamo.marcarCompensacionRequerida();

        assertThat(prestamo.estado())
                .isEqualTo(EstadoPrestamo.COMPENSACION_REQUERIDA);

        assertThat(prestamo.reservaId())
                .isEqualTo(RESERVA_ID);
    }

    @Test
    void registraDevolucionValida() {
        Prestamo prestamo = crearPrestamoActivo();
        LocalDate fechaDevolucion = FECHA_PRESTAMO.plusDays(5);

        prestamo.registrarDevolucion(fechaDevolucion);

        assertThat(prestamo.estado())
                .isEqualTo(EstadoPrestamo.DEVUELTO);

        assertThat(prestamo.fechaDevolucion())
                .isEqualTo(fechaDevolucion);
    }

    @Test
    void rechazaSegundaDevolucion() {
        Prestamo prestamo = crearPrestamoActivo();

        prestamo.registrarDevolucion(FECHA_PRESTAMO.plusDays(5));

        assertThrows(
                PrestamoYaDevueltoException.class,
                () -> prestamo.registrarDevolucion(
                        FECHA_PRESTAMO.plusDays(6)
                )
        );
    }

    @Test
    void rechazaActivacionDosVeces() {
        Prestamo prestamo = crearPrestamoActivo();

        assertThrows(
                PrestamoEstadoNoPermitidoException.class,
                prestamo::confirmarReserva
        );
    }

    @Test
    void noPermiteCambiarUnaIdentidadAsignada() {
        Prestamo prestamo = crearPrestamoPendiente()
                .conId(new PrestamoId(100L));

        assertThrows(
                IllegalStateException.class,
                () -> prestamo.conId(new PrestamoId(200L))
        );
    }

    @Test
    void creaPrestamoPendienteConPlazoDeQuinceDias() {
        Prestamo prestamo = crearPrestamoPendiente();

        assertThat(prestamo.estado())
                .isEqualTo(EstadoPrestamo.PENDIENTE_CONFIRMACION);

        assertThat(prestamo.fechaPrestamo())
                .isEqualTo(FECHA_PRESTAMO);

        assertThat(prestamo.fechaLimite())
                .isEqualTo(FECHA_PRESTAMO.plusDays(15));

        assertThat(prestamo.id()).isEmpty();
    }

    @Test
    void rechazaIdentificadoresNoPositivos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ClienteId(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ClienteId(-1L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new LibroId(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PrestamoId(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PrestamoId(-1L)
        );
    }
}
