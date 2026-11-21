package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.entity.EstadoPrestamo;
import com.ingsoft.biblioteca.entity.EstadoReservaPrestamo;
import com.ingsoft.biblioteca.entity.ReservaPrestamo;
import com.ingsoft.biblioteca.repository.LibroRepository;
import com.ingsoft.biblioteca.repository.PrestamoRepository;
import com.ingsoft.biblioteca.repository.ReservaPrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReservaPrestamoService {
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final ReservaPrestamoRepository reservaRepository;

    public ReservaPrestamoService(LibroRepository libroRepository, PrestamoRepository prestamoRepository,
                                  ReservaPrestamoRepository reservaRepository) {
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional
    public ReservaPrestamo crear(Long libroId) {
        var libro = libroRepository.buscarPorIdConBloqueo(libroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El libro no existe."));
        if (!libro.isActivo() || prestamoRepository.existsByLibroIdAndEstado(libroId, EstadoPrestamo.ACTIVO)
                || reservaRepository.existsByLibroIdAndEstado(libroId, EstadoReservaPrestamo.VIGENTE)) {
            throw new ConflictoReservaException("El libro no está disponible para reserva.");
        }
        return reservaRepository.save(new ReservaPrestamo(UUID.randomUUID().toString(), libro));
    }

    @Transactional(readOnly = true)
    public ReservaPrestamo consultar(String reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva no existe."));
    }

    @Transactional
    public ReservaPrestamo confirmar(String reservaId) {
        ReservaPrestamo reserva = consultar(reservaId);
        if (reserva.getEstado() == EstadoReservaPrestamo.CONFIRMADA) return reserva;
        if (reserva.getEstado() != EstadoReservaPrestamo.VIGENTE) throw new ConflictoReservaException("La reserva no puede confirmarse.");
        reserva.setEstado(EstadoReservaPrestamo.CONFIRMADA);
        return reserva;
    }

    @Transactional
    public ReservaPrestamo cancelar(String reservaId) {
        ReservaPrestamo reserva = consultar(reservaId);
        if (reserva.getEstado() == EstadoReservaPrestamo.CANCELADA) return reserva;
        if (reserva.getEstado() == EstadoReservaPrestamo.CONFIRMADA) throw new ConflictoReservaException("La reserva no puede cancelarse.");
        reserva.setEstado(EstadoReservaPrestamo.CANCELADA);
        return reserva;
    }

    public static class RecursoNoEncontradoException extends RuntimeException { public RecursoNoEncontradoException(String m) { super(m); } }
    public static class ConflictoReservaException extends RuntimeException { public ConflictoReservaException(String m) { super(m); } }
}
