package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.entity.*;
import com.ingsoft.biblioteca.repository.MultaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class MultaService {

    private static final BigDecimal TARIFA_DIARIA =
            new BigDecimal("1000.00");

    private final MultaRepository multaRepository;

    public MultaService(MultaRepository multaRepository) {
        this.multaRepository = multaRepository;
    }

    public long calcularDiasRetraso(
            LocalDate fechaLimite,
            LocalDate fechaEvaluacion
    ) {
        if (fechaLimite == null || fechaEvaluacion == null) {
            throw new IllegalArgumentException(
                    "Las fechas son obligatorias."
            );
        }

        return Math.max(
                0,
                ChronoUnit.DAYS.between(
                        fechaLimite,
                        fechaEvaluacion
                )
        );
    }

    public BigDecimal calcularMultaEstimada(
            Prestamo prestamo,
            LocalDate fechaEvaluacion
    ) {
        long dias = calcularDiasRetraso(
                prestamo.getFechaLimite(),
                fechaEvaluacion
        );

        return TARIFA_DIARIA.multiply(
                BigDecimal.valueOf(dias)
        );
    }

    @Transactional
    public Multa registrarMultaDefinitiva(
            Prestamo prestamo,
            LocalDate fechaDevolucion
    ) {
        return multaRepository.findByPrestamoId(prestamo.getId())
                .orElseGet(() -> {
                    long dias = calcularDiasRetraso(
                            prestamo.getFechaLimite(),
                            fechaDevolucion
                    );

                    Multa multa = new Multa();
                    multa.setPrestamo(prestamo);
                    multa.setTarifaDiaria(TARIFA_DIARIA);
                    multa.setDiasRetrasoFinales((int) dias);
                    multa.setImporteTotal(
                            TARIFA_DIARIA.multiply(
                                    BigDecimal.valueOf(dias)
                            )
                    );
                    multa.setFechaCalculo(fechaDevolucion);

                    multa.setEstado(
                            dias == 0
                                    ? EstadoMulta.PAGADA
                                    : EstadoMulta.PENDIENTE
                    );

                    return multaRepository.save(multa);
                });
    }

    @Transactional(readOnly = true)
    public BigDecimal consultarMultaEstimada(
            Prestamo prestamo
    ) {
        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            return multaRepository.findByPrestamoId(prestamo.getId())
                    .map(Multa::getImporteTotal)
                    .orElse(BigDecimal.ZERO);
        }

        return calcularMultaEstimada(
                prestamo,
                LocalDate.now()
        );
    }
}