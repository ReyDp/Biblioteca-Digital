
package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

import com.ingsoft.prestamos.application.port.input.RegistrarDevolucionUseCase;
import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.EstadoPrestamo;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;
import com.ingsoft.prestamos.application.port.output.PrestamoPersistencePort;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.aop.proxy-target-class=false"
        }
)
@EnabledIfSystemProperty(
        named = "postgres.integration",
        matches = "true"
)
class PrestamosPostgresIntegrationTest {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private PrestamoPersistencePort prestamoPersistenceAdapter;

    @Autowired
    private RegistrarDevolucionUseCase registrarDevolucionUseCase;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void limpiarDatosDePrueba() {
        outboxEventRepository.deleteAllInBatch();
        prestamoRepository.deleteAllInBatch();
    }

    @Test
    void falloAlInsertarEventoRevierteCambioDelPrestamo() {

        assertThrows(
                DataIntegrityViolationException.class,
                () -> transactionTemplate.executeWithoutResult(status -> {

                    prestamoPersistenceAdapter.guardar(
                            crearPrestamoActivo(1L, 100L)
                    );

                    // Evento inválido: incumple las restricciones NOT NULL.
                    outboxEventRepository.saveAndFlush(
                            new OutboxEventEntity()
                    );
                })
        );

        assertEquals(0, prestamoRepository.count());
        assertEquals(0, outboxEventRepository.count());
    }

    @Test
    void falloAlGuardarPrestamoRevierteEventoDeLaMismaTransaccion() {

        assertThrows(
                DataIntegrityViolationException.class,
                () -> transactionTemplate.executeWithoutResult(status -> {

                    outboxEventRepository.saveAndFlush(
                            crearEventoValido(UUID.randomUUID(), 100L)
                    );

                    // Préstamo inválido: incumple las restricciones NOT NULL.
                    prestamoRepository.saveAndFlush(
                            new PrestamoEntity()
                    );
                })
        );

        assertEquals(0, prestamoRepository.count());
        assertEquals(0, outboxEventRepository.count());
    }

    @Test
    void guardaDevolucionYEventoOutboxConElMismoId() {

        Prestamo guardado = prestamoPersistenceAdapter.guardar(
                crearPrestamoActivo(10L, 200L)
        );

        Long prestamoId = guardado.id()
                .orElseThrow()
                .value();

        Prestamo devuelto = registrarDevolucionUseCase.registrar(
                new PrestamoId(prestamoId)
        );

        assertEquals(
                EstadoPrestamo.DEVUELTO,
                devuelto.estado()
        );

        assertNotNull(devuelto.fechaDevolucion());

        PrestamoEntity prestamoPersistido =
                prestamoRepository.findById(prestamoId)
                        .orElseThrow();

        assertEquals(
                EstadoPrestamo.DEVUELTO,
                prestamoPersistido.getEstado()
        );

        assertEquals(
                1,
                outboxEventRepository.count()
        );

        OutboxEventEntity evento =
                outboxEventRepository.findAll().get(0);

        assertEquals(prestamoId, evento.getAggregateId());
        assertEquals("PrestamoDevuelto", evento.getEventType());
        assertEquals(OutboxStatus.PENDIENTE, evento.getStatus());
    }

    @Test
    void insertaRecuperaYRecibeIdBigintDePostgres() {

        Prestamo resultado = prestamoPersistenceAdapter.guardar(
                crearPrestamoActivo(20L, 300L)
        );

        Long id = resultado.id()
                .orElseThrow()
                .value();

        assertTrue(id > 0L);

        PrestamoEntity recuperado =
                prestamoRepository.findById(id)
                        .orElseThrow();

        assertEquals(id, recuperado.getId());
        assertEquals(20L, recuperado.getClienteId());
        assertEquals(300L, recuperado.getLibroId());
        assertEquals(
                EstadoPrestamo.ACTIVO,
                recuperado.getEstado()
        );
    }

    @Test
    void eventIdUnicoSeImponeEnPostgres() {

        UUID eventId = UUID.randomUUID();

        outboxEventRepository.saveAndFlush(
                crearEventoValido(eventId, 400L)
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> outboxEventRepository.saveAndFlush(
                        crearEventoValido(eventId, 401L)
                )
        );

        assertEquals(1, outboxEventRepository.count());
    }

    @Test
    void indiceParcialImpideDosPrestamosOcupantesYDevueltoLiberaLibro() {

        Prestamo primero = prestamoPersistenceAdapter.guardar(
                crearPrestamoActivo(30L, 500L)
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> prestamoPersistenceAdapter.guardar(
                        crearPrestamoActivo(31L, 500L)
                )
        );

        Long primerId = primero.id()
                .orElseThrow()
                .value();

        Prestamo prestamoParaDevolver =
                prestamoRepository.findById(primerId)
                        .map(entity -> new PrestamoPersistenceMapper()
                                .toDomain(entity))
                        .orElseThrow();

        prestamoParaDevolver.registrarDevolucion(
                LocalDate.now(ZoneOffset.UTC)
        );

        prestamoPersistenceAdapter.guardar(prestamoParaDevolver);

        Prestamo segundo = prestamoPersistenceAdapter.guardar(
                crearPrestamoActivo(31L, 500L)
        );

        assertNotNull(segundo.id().orElseThrow());

        assertEquals(
                EstadoPrestamo.DEVUELTO,
                prestamoRepository.findById(primerId)
                        .orElseThrow()
                        .getEstado()
        );

        assertEquals(2, prestamoRepository.count());
    }

    private Prestamo crearPrestamoActivo(
            Long clienteId,
            Long libroId
    ) {
        Prestamo prestamo = Prestamo.crearPendiente(
                new ClienteId(clienteId),
                new LibroId(libroId),
                new ReservaId("reserva-" + clienteId + "-" + libroId),
                LocalDate.now(ZoneOffset.UTC)
        );

        prestamo.confirmarReserva();

        return prestamo;
    }

    private OutboxEventEntity crearEventoValido(
            UUID eventId,
            Long aggregateId
    ) {
        Instant ahora = Instant.now();

        OutboxEventEntity evento = new OutboxEventEntity();
        evento.setEventId(eventId);
        evento.setEventType("PrestamoDevuelto");
        evento.setEventVersion(1);
        evento.setAggregateId(aggregateId);
        evento.setPayload("{\"prestamoId\":" + aggregateId + "}");
        evento.setStatus(OutboxStatus.PENDIENTE);
        evento.setAttempts(0);
        evento.setCreatedAt(ahora);
        evento.setAvailableAt(ahora);

        return evento;
    }
}

