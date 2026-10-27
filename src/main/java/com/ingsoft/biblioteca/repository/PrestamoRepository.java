package com.ingsoft.biblioteca.repository;

import com.ingsoft.biblioteca.entity.Prestamo;
import com.ingsoft.biblioteca.entity.EstadoPrestamo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByClienteId(Long clienteId);

    List<Prestamo> findByEstado(EstadoPrestamo estado);

    boolean existsByLibroIdAndEstado(
            Long libroId,
            EstadoPrestamo estado
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Prestamo p WHERE p.id = :id")
    Optional<Prestamo> buscarPorIdConBloqueo(@Param("id") Long id);
}
