package com.ingsoft.biblioteca.repository;

import com.ingsoft.biblioteca.entity.EstadoReservaPrestamo;
import com.ingsoft.biblioteca.entity.ReservaPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaPrestamoRepository extends JpaRepository<ReservaPrestamo, String> {
    boolean existsByLibroIdAndEstado(Long libroId, EstadoReservaPrestamo estado);
}
