package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.dto.DashboardStats;
import com.ingsoft.biblioteca.entity.EstadoMulta;
import com.ingsoft.biblioteca.entity.EstadoPrestamo;
import com.ingsoft.biblioteca.repository.LibroRepository;
import com.ingsoft.biblioteca.repository.PagoMultaRepository;
import com.ingsoft.biblioteca.repository.PrestamoRepository;
import com.ingsoft.biblioteca.repository.MultaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class DashboardService {

    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;
    private final PagoMultaRepository pagoMultaRepository;

    public DashboardService(
            LibroRepository libroRepository,
            PrestamoRepository prestamoRepository,
            MultaRepository multaRepository,
            PagoMultaRepository pagoMultaRepository
    ) {
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.multaRepository = multaRepository;
        this.pagoMultaRepository = pagoMultaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStats obtenerEstadisticas() {
        DashboardStats stats = new DashboardStats();

        // Ejemplares
        stats.setTotalEjemplares(libroRepository.count());
        stats.setEjemplaresInactivos(libroRepository.countByActivoFalse());

        // Préstamos
        long prestamosActivos = prestamoRepository.countByEstado(EstadoPrestamo.ACTIVO);
        stats.setPrestamosActivos(prestamosActivos);

        long prestamosVencidos = prestamoRepository.countByEstadoAndFechaLimiteBefore(
                EstadoPrestamo.ACTIVO, LocalDate.now()
        );
        stats.setPrestamosVencidos(prestamosVencidos);

        stats.setPrestamosDevueltos(prestamoRepository.countByEstado(EstadoPrestamo.DEVUELTO));

        // Ejemplares disponibles: activos menos aquellos con préstamo activo
        // Cada préstamo activo ocupa exactamente un ejemplar activo
        long ejemplaresActivos = libroRepository.countByActivoTrue();
        stats.setEjemplaresDisponibles(ejemplaresActivos - prestamosActivos);

        // Multas
        stats.setMultasPendientes(multaRepository.countByEstadoNot(EstadoMulta.PAGADA));

        // Saldo total pendiente: suma de importes de multas no pagadas menos los pagos realizados
        BigDecimal importeTotalMultasNoPagadas = multaRepository.sumarImporteTotalByEstadoNot(
                EstadoMulta.PAGADA
        );
        BigDecimal totalPagosRealizados = pagoMultaRepository.sumarPagosByMultaEstadoNot(
                EstadoMulta.PAGADA
        );
        stats.setSaldoTotalPendiente(importeTotalMultasNoPagadas.subtract(totalPagosRealizados));

        return stats;
    }
}
