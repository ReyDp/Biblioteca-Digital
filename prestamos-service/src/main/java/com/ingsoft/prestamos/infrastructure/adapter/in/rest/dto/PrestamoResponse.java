
package com.ingsoft.prestamos.infrastructure.adapter.in.rest.dto;

import com.ingsoft.prestamos.domain.model.Prestamo;
import java.time.LocalDate;

public record PrestamoResponse(Long id, Long clienteId, Long libroId, LocalDate fechaPrestamo, LocalDate fechaLimite, LocalDate fechaDevolucion, String estado) {
    public static PrestamoResponse desde(Prestamo prestamo) {
        return new PrestamoResponse(prestamo.id().map(id -> id.value()).orElse(null), prestamo.clienteId().value(), prestamo.libroId().value(), prestamo.fechaPrestamo(), prestamo.fechaLimite(), prestamo.fechaDevolucion(), prestamo.estado().name());
    }
}

