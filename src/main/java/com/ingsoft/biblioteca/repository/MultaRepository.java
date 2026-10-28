package com.ingsoft.biblioteca.repository;

import com.ingsoft.biblioteca.entity.Multa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

public interface MultaRepository extends JpaRepository<Multa, Long> {

    Optional<Multa> findByPrestamoId(Long prestamoId);

    @Query("""
            SELECT m FROM Multa m
            JOIN FETCH m.prestamo p
            JOIN FETCH p.libro
            JOIN FETCH p.cliente c
            JOIN FETCH c.usuario
            WHERE c.id = :clienteId
            ORDER BY m.fechaCalculo DESC
            """)
    List<Multa> buscarPorClienteIdConDetalles(@Param("clienteId") Long clienteId);

    @Query("""
            SELECT m FROM Multa m
            JOIN FETCH m.prestamo p
            JOIN FETCH p.libro
            JOIN FETCH p.cliente c
            JOIN FETCH c.usuario
            WHERE m.id = :multaId AND c.id = :clienteId
            """)
    Optional<Multa> buscarPorIdYClienteIdConDetalles(
            @Param("multaId") Long multaId,
            @Param("clienteId") Long clienteId
    );

    @Query("""
            SELECT m FROM Multa m
            JOIN FETCH m.prestamo p
            JOIN FETCH p.libro
            JOIN FETCH p.cliente c
            JOIN FETCH c.usuario
            ORDER BY m.estado ASC, m.fechaCalculo DESC
            """)
    List<Multa> buscarTodasConDetalles();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Multa m WHERE m.id = :id")
    Optional<Multa> buscarPorIdConBloqueo(@Param("id") Long id);

    long countByEstadoNot(com.ingsoft.biblioteca.entity.EstadoMulta estado);

    @Query("""
            SELECT COALESCE(SUM(m.importeTotal), 0)
            FROM Multa m
            WHERE m.estado != :estadoExcluido
            """)
    BigDecimal sumarImporteTotalByEstadoNot(@Param("estadoExcluido") com.ingsoft.biblioteca.entity.EstadoMulta estadoExcluido);
}
