package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.EstadoPrestamo;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PrestamoPersistenceMapperTest {

    private final PrestamoPersistenceMapper mapper =
            new PrestamoPersistenceMapper();

    @Test
    void conservaIdentidadRelacionesFechasYEstado() {
        // Arrange
        LocalDate fechaPrestamo = LocalDate.of(2026, 10, 1);
        LocalDate fechaLimite = LocalDate.of(2026, 10, 16);
        LocalDate fechaDevolucion = LocalDate.of(2026, 10, 10);

        Prestamo prestamo = Prestamo.rehidratar(
                new PrestamoId(150L),
                new ClienteId(25L),
                new LibroId(80L),
                new ReservaId("reserva-150"),
                fechaPrestamo,
                fechaLimite,
                fechaDevolucion,
                EstadoPrestamo.DEVUELTO
        );

        PrestamoEntity entity = new PrestamoEntity();

        // Act
        PrestamoEntity resultadoEntity = mapper.toEntity(prestamo, entity);
        Prestamo resultadoDomain = mapper.toDomain(resultadoEntity);

        // Assert
        assertNotNull(resultadoEntity);

        assertEquals(150L, resultadoEntity.getId());
        assertEquals(25L, resultadoEntity.getClienteId());
        assertEquals(80L, resultadoEntity.getLibroId());
        assertEquals("reserva-150", resultadoEntity.getReservaId());
        assertEquals(fechaPrestamo, resultadoEntity.getFechaPrestamo());
        assertEquals(fechaLimite, resultadoEntity.getFechaLimite());
        assertEquals(fechaDevolucion, resultadoEntity.getFechaDevolucion());
        assertEquals(EstadoPrestamo.DEVUELTO, resultadoEntity.getEstado());

        assertEquals(prestamo.id(), resultadoDomain.id());
        assertEquals(prestamo.clienteId(), resultadoDomain.clienteId());
        assertEquals(prestamo.libroId(), resultadoDomain.libroId());
        assertEquals(prestamo.reservaId(), resultadoDomain.reservaId());
        assertEquals(prestamo.fechaPrestamo(), resultadoDomain.fechaPrestamo());
        assertEquals(prestamo.fechaLimite(), resultadoDomain.fechaLimite());
        assertEquals(prestamo.fechaDevolucion(), resultadoDomain.fechaDevolucion());
        assertEquals(prestamo.estado(), resultadoDomain.estado());
    }
}