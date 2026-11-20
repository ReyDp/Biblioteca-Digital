
package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

import com.ingsoft.prestamos.application.port.output.PrestamoPersistencePort;
import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.EstadoPrestamo;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PrestamoPersistenceAdapter implements PrestamoPersistencePort {

    private static final List<EstadoPrestamo> ESTADOS_OCUPANTES = List.of(
            EstadoPrestamo.PENDIENTE_CONFIRMACION,
            EstadoPrestamo.ACTIVO,
            EstadoPrestamo.COMPENSACION_REQUERIDA
    );

    private static final List<EstadoPrestamo> ESTADOS_RECONCILIACION = List.of(
            EstadoPrestamo.PENDIENTE_CONFIRMACION,
            EstadoPrestamo.COMPENSACION_REQUERIDA
    );

    private final PrestamoRepository repository;
    private final PrestamoPersistenceMapper mapper;

    public PrestamoPersistenceAdapter(
            PrestamoRepository repository,
            PrestamoPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Prestamo guardar(Prestamo prestamo) {
        PrestamoEntity entity = mapper.toEntity(
                prestamo,
                new PrestamoEntity()
        );

        // La reserva se confirma después de persistir: se requiere el ID definitivo antes de la llamada remota.
        PrestamoEntity saved = repository.saveAndFlush(entity);

        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Prestamo> buscarPorId(PrestamoId id) {
        return repository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<Prestamo> buscarPorIdConBloqueo(PrestamoId id) {
        return repository.findByIdForUpdate(id.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> buscarPorCliente(ClienteId clienteId) {
        return repository.findByClienteIdOrderByFechaPrestamoDesc(
                        clienteId.value()
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePrestamoQueOcupaLibro(LibroId libroId) {
        return repository.existsByLibroIdAndEstadoIn(
                libroId.value(),
                ESTADOS_OCUPANTES
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> buscarPendientesDeReconciliacion() {
        return repository.findByEstadoIn(ESTADOS_RECONCILIACION)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
