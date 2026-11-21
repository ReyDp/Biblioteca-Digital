package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.entity.ReservaPrestamo;
import com.ingsoft.biblioteca.repository.ClienteRepository;
import com.ingsoft.biblioteca.service.ReservaPrestamoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServicioPrestamosApiController {
    private final ClienteRepository clienteRepository;
    private final ReservaPrestamoService reservas;

    public ServicioPrestamosApiController(ClienteRepository clienteRepository, ReservaPrestamoService reservas) {
        this.clienteRepository = clienteRepository;
        this.reservas = reservas;
    }

    @GetMapping("/api/clientes/{clienteId}/estado-prestamo")
    ClienteExisteResponse clienteExiste(@PathVariable Long clienteId) {
        return new ClienteExisteResponse(clienteRepository.existsById(clienteId));
    }

    @PostMapping("/api/libros/{libroId}/reservas-prestamo")
    ResponseEntity<ReservaResponse> crearReserva(@PathVariable Long libroId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservaResponse.desde(reservas.crear(libroId)));
    }

    @GetMapping("/api/libros/reservas-prestamo/{reservaId}")
    ReservaResponse consultarReserva(@PathVariable String reservaId) { return ReservaResponse.desde(reservas.consultar(reservaId)); }

    @PostMapping("/api/libros/reservas-prestamo/{reservaId}/confirmar")
    ReservaResponse confirmarReserva(@PathVariable String reservaId) { return ReservaResponse.desde(reservas.confirmar(reservaId)); }

    @PostMapping("/api/libros/reservas-prestamo/{reservaId}/cancelar")
    ReservaResponse cancelarReserva(@PathVariable String reservaId) { return ReservaResponse.desde(reservas.cancelar(reservaId)); }

    @ExceptionHandler(ReservaPrestamoService.RecursoNoEncontradoException.class)
    ResponseEntity<Void> notFound() { return ResponseEntity.notFound().build(); }
    @ExceptionHandler(ReservaPrestamoService.ConflictoReservaException.class)
    ResponseEntity<Void> conflict() { return ResponseEntity.status(HttpStatus.CONFLICT).build(); }

    record ClienteExisteResponse(boolean existe) { }
    record ReservaResponse(String reservaId, Long libroId, String estado) {
        static ReservaResponse desde(ReservaPrestamo reserva) {
            return new ReservaResponse(reserva.getId(), reserva.getLibro().getId(), reserva.getEstado().name());
        }
    }
}
