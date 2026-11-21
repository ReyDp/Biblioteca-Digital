
package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

import com.ingsoft.prestamos.domain.model.EstadoPrestamo;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrestamoRepository
        extends JpaRepository<PrestamoEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PrestamoEntity p where p.id = :id")
    Optional<PrestamoEntity> findByIdForUpdate(@Param("id") Long id);

    List<PrestamoEntity> findByClienteIdOrderByFechaPrestamoDesc(
            Long clienteId
    );

    boolean existsByLibroIdAndEstadoIn(
            Long libroId,
            Collection<EstadoPrestamo> estados
    );

    List<PrestamoEntity> findByEstadoIn(
            Collection<EstadoPrestamo> estados
    );
}
