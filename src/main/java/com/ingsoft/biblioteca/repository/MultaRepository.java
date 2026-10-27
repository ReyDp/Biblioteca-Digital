package com.ingsoft.biblioteca.repository;

import com.ingsoft.biblioteca.entity.Multa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MultaRepository extends JpaRepository<Multa, Long> {

    Optional<Multa> findByPrestamoId(Long prestamoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Multa m WHERE m.id = :id")
    Optional<Multa> buscarPorIdConBloqueo(@Param("id") Long id);
}