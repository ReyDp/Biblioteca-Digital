package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestamoPersistenceAdapterTest {

    @Mock
    private PrestamoRepository repository;

    private PrestamoPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        PrestamoPersistenceMapper mapper = new PrestamoPersistenceMapper();
        adapter = new PrestamoPersistenceAdapter(repository, mapper);
    }

    @Test
    void guardaUnPrestamoNuevoYDevuelveElIdAsignado() {
        // Arrange
        LocalDate fechaPrestamo = LocalDate.of(2026, 11, 19);

        Prestamo prestamo = Prestamo.crearPendiente(
                new ClienteId(1L),
                new LibroId(10L),
                new ReservaId("reserva-001"),
                fechaPrestamo
        );

        when(repository.saveAndFlush(any(PrestamoEntity.class)))
                .thenAnswer(invocation -> {
                    PrestamoEntity entity = invocation.getArgument(0);
                    entity.setId(101L);
                    return entity;
                });

        // Act
        Prestamo resultado = adapter.guardar(prestamo);

        // Assert
        assertEquals(Optional.of(new PrestamoId(101L)), resultado.id());
        assertEquals(new ClienteId(1L), resultado.clienteId());
        assertEquals(new LibroId(10L), resultado.libroId());
        assertEquals(new ReservaId("reserva-001"), resultado.reservaId());
        assertEquals(EstadoPrestamo.PENDIENTE_CONFIRMACION, resultado.estado());

        verify(repository).saveAndFlush(any(PrestamoEntity.class));
    }

    @Test
    void consultaBloqueadaUsaElRepositorioConBloqueo() {
        // Arrange
        Long prestamoId = 100L;

        PrestamoEntity entity = new PrestamoEntity();
        entity.setId(prestamoId);
        entity.setClienteId(1L);
        entity.setLibroId(10L);
        entity.setReservaId("reserva-001");
        entity.setFechaPrestamo(LocalDate.of(2026, 11, 19));
        entity.setFechaLimite(LocalDate.of(2026, 12, 4));
        entity.setFechaDevolucion(null);
        entity.setEstado(EstadoPrestamo.ACTIVO);

        when(repository.findByIdForUpdate(prestamoId))
                .thenReturn(Optional.of(entity));

        // Act
        Optional<Prestamo> resultado =
                adapter.buscarPorIdConBloqueo(new PrestamoId(prestamoId));

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(Optional.of(new PrestamoId(prestamoId)), resultado.get().id());
        assertEquals(EstadoPrestamo.ACTIVO, resultado.get().estado());

        verify(repository).findByIdForUpdate(prestamoId);
    }

    @Test
    void consultaOcupacionIncluyeEstadosOperativos() {
        // Arrange
        Long libroId = 10L;

        when(repository.existsByLibroIdAndEstadoIn(
                eq(libroId),
                argThat(estados ->
                        estados.size() == 3
                                && estados.containsAll(List.of(
                                EstadoPrestamo.PENDIENTE_CONFIRMACION,
                                EstadoPrestamo.ACTIVO,
                                EstadoPrestamo.COMPENSACION_REQUERIDA
                        ))
                )
        )).thenReturn(true);

        // Act
        boolean resultado =
                adapter.existePrestamoQueOcupaLibro(new LibroId(libroId));

        // Assert
        assertTrue(resultado);

        verify(repository).existsByLibroIdAndEstadoIn(
                eq(libroId),
                argThat(estados ->
                        estados.contains(EstadoPrestamo.PENDIENTE_CONFIRMACION)
                                && estados.contains(EstadoPrestamo.ACTIVO)
                                && estados.contains(EstadoPrestamo.COMPENSACION_REQUERIDA)
                                && !estados.contains(EstadoPrestamo.DEVUELTO)
                )
        );
    }

    @Test
    void consultaReconciliacionExcluyePrestamosActivos() {
        when(repository.findByEstadoIn(argThat(estados ->
                estados.size() == 2
                        && estados.contains(EstadoPrestamo.PENDIENTE_CONFIRMACION)
                        && estados.contains(EstadoPrestamo.COMPENSACION_REQUERIDA)
                        && !estados.contains(EstadoPrestamo.ACTIVO))))
                .thenReturn(List.of());

        assertTrue(adapter.buscarPendientesDeReconciliacion().isEmpty());

        verify(repository).findByEstadoIn(argThat(estados ->
                estados.size() == 2
                        && estados.contains(EstadoPrestamo.PENDIENTE_CONFIRMACION)
                        && estados.contains(EstadoPrestamo.COMPENSACION_REQUERIDA)
                        && !estados.contains(EstadoPrestamo.ACTIVO)));
    }
}
