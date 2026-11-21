/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package com.ingsoft.prestamos.infrastructure.adapter.output.persistence;

import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.PrestamoEntity;
import org.springframework.stereotype.Component;

@Component
public class PrestamoPersistenceMapper {
    public Prestamo toDomain(PrestamoEntity entity) {
        return Prestamo.rehidratar(PrestamoId.asignado(entity.getId()), new ClienteId(entity.getClienteId()), new LibroId(entity.getLibroId()), new ReservaId(entity.getReservaId()), entity.getFechaPrestamo(), entity.getFechaLimite(), entity.getFechaDevolucion(), entity.getEstado());
    }

    public PrestamoEntity toEntity(Prestamo domain, PrestamoEntity target) {
        domain.id().ifPresent(id -> target.setId(id.value()));
        target.setClienteId(domain.clienteId().value());
        target.setLibroId(domain.libroId().value());
        target.setReservaId(domain.reservaId().value());
        target.setFechaPrestamo(domain.fechaPrestamo());
        target.setFechaLimite(domain.fechaLimite());
        target.setFechaDevolucion(domain.fechaDevolucion());
        target.setEstado(domain.estado());
        return target;
    }
}

