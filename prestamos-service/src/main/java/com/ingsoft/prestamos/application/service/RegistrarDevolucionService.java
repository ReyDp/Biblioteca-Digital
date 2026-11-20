
package com.ingsoft.prestamos.application.service;

import com.ingsoft.prestamos.application.port.input.RegistrarDevolucionUseCase;
import com.ingsoft.prestamos.application.port.output.PrestamoPersistencePort;
import com.ingsoft.prestamos.application.port.output.PublicarPrestamoDevueltoPort;
import com.ingsoft.prestamos.domain.exception.PrestamoNoExisteException;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

public class RegistrarDevolucionService
implements RegistrarDevolucionUseCase {
    private final PrestamoPersistencePort prestamoPersistence;
    private final PublicarPrestamoDevueltoPort publicador;
    private final Clock clock;

    public RegistrarDevolucionService(PrestamoPersistencePort prestamoPersistence, PublicarPrestamoDevueltoPort publicador, Clock clock) {
        this.prestamoPersistence = Objects.requireNonNull(prestamoPersistence);
        this.publicador = Objects.requireNonNull(publicador);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    @Transactional
    public Prestamo registrar(PrestamoId prestamoId) {
        Prestamo prestamo = this.prestamoPersistence.buscarPorIdConBloqueo(prestamoId).orElseThrow(PrestamoNoExisteException::new);
        prestamo.registrarDevolucion(LocalDate.now(this.clock));
        Prestamo actualizado = this.prestamoPersistence.guardar(prestamo);
        this.publicador.publicar(actualizado);
        return actualizado;
    }
}

