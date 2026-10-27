package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.entity.*;
import com.ingsoft.biblioteca.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrestamoService {

    private static final int DURACION_DIAS = 15;

    private final PrestamoRepository prestamoRepository;
    private final ClienteRepository clienteRepository;
    private final LibroRepository libroRepository;
    private final MultaService multaService;

    public PrestamoService(
            PrestamoRepository prestamoRepository,
            ClienteRepository clienteRepository,
            LibroRepository libroRepository,
            MultaService multaService
    ) {
        this.prestamoRepository = prestamoRepository;
        this.clienteRepository = clienteRepository;
        this.libroRepository = libroRepository;
        this.multaService = multaService;
    }

    @Transactional
    public Prestamo registrarPrestamo(
            Long clienteId,
            Long libroId
    ) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El cliente no existe."
                        )
                );

        Libro libro = libroRepository.buscarPorIdConBloqueo(libroId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El libro no existe."
                        )
                );

        if (!libro.isActivo()) {
            throw new IllegalStateException(
                    "El libro no está disponible."
            );
        }

        boolean tienePrestamoActivo =
                prestamoRepository.existsByLibroIdAndEstado(
                        libroId,
                        EstadoPrestamo.ACTIVO
                );

        if (tienePrestamoActivo) {
            throw new IllegalStateException(
                    "El libro ya tiene un préstamo activo."
            );
        }

        LocalDate fechaPrestamo = LocalDate.now();

        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(fechaPrestamo);
        prestamo.setFechaLimite(
                fechaPrestamo.plusDays(DURACION_DIAS)
        );
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        return prestamoRepository.save(prestamo);
    }

    @Transactional
    public Prestamo registrarDevolucion(Long prestamoId) {
        Prestamo prestamo =
                prestamoRepository.buscarPorIdConBloqueo(prestamoId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El préstamo no existe."
                                )
                        );

        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new IllegalStateException(
                    "Este préstamo ya fue devuelto."
            );
        }

        LocalDate fechaDevolucion = LocalDate.now();

        prestamo.setFechaDevolucion(fechaDevolucion);
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);

        prestamoRepository.save(prestamo);

        multaService.registrarMultaDefinitiva(
                prestamo,
                fechaDevolucion
        );

        return prestamo;
    }

    @Transactional(readOnly = true)
    public List<Prestamo> listarPrestamosDeCliente(
            Long clienteId
    ) {
        return prestamoRepository.findByClienteId(clienteId);
    }
}