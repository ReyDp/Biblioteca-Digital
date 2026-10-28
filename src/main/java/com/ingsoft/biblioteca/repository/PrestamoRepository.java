package com.ingsoft.biblioteca.repository;

import com.ingsoft.biblioteca.entity.Prestamo;
import com.ingsoft.biblioteca.entity.EstadoPrestamo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    @Query("""
            SELECT p FROM Prestamo p
            JOIN FETCH p.cliente c
            JOIN FETCH c.usuario
            JOIN FETCH p.libro
            WHERE c.id = :clienteId
            ORDER BY p.fechaPrestamo DESC
            """)
    List<Prestamo> buscarPorClienteIdConDetalles(@Param("clienteId") Long clienteId);

    @Query("""
            SELECT p FROM Prestamo p
            JOIN FETCH p.cliente c
            JOIN FETCH c.usuario
            JOIN FETCH p.libro
            WHERE p.id = :prestamoId AND c.id = :clienteId
            """)
    Optional<Prestamo> buscarPorIdYClienteIdConDetalles(
            @Param("prestamoId") Long prestamoId,
            @Param("clienteId") Long clienteId
    );

    @Query("""
            SELECT p FROM Prestamo p
            JOIN FETCH p.cliente c
            JOIN FETCH c.usuario
            JOIN FETCH p.libro
            WHERE p.estado = :estado
            ORDER BY p.fechaLimite ASC
            """)
    List<Prestamo> buscarPorEstadoConDetalles(@Param("estado") EstadoPrestamo estado);

    List<Prestamo> findByEstado(EstadoPrestamo estado);

    boolean existsByLibroIdAndEstado(
            Long libroId,
            EstadoPrestamo estado
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Prestamo p WHERE p.id = :id")
    Optional<Prestamo> buscarPorIdConBloqueo(@Param("id") Long id);

    long countByEstado(EstadoPrestamo estado);

    long countByEstadoAndFechaLimiteBefore(EstadoPrestamo estado, LocalDate fecha);
}
