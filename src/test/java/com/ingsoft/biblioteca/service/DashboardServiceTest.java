package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.dto.DashboardStats;
import com.ingsoft.biblioteca.entity.EstadoMulta;
import com.ingsoft.biblioteca.entity.EstadoPrestamo;
import com.ingsoft.biblioteca.repository.LibroRepository;
import com.ingsoft.biblioteca.repository.MultaRepository;
import com.ingsoft.biblioteca.repository.PagoMultaRepository;
import com.ingsoft.biblioteca.repository.PrestamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private MultaRepository multaRepository;

    @Mock
    private PagoMultaRepository pagoMultaRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                libroRepository,
                prestamoRepository,
                multaRepository,
                pagoMultaRepository
        );
    }

    @Test
    void dashboardConDatosVaciosRetornaCeros() {
        when(libroRepository.count()).thenReturn(0L);
        when(libroRepository.countByActivoTrue()).thenReturn(0L);
        when(libroRepository.countByActivoFalse()).thenReturn(0L);
        when(prestamoRepository.countByEstado(any(EstadoPrestamo.class))).thenReturn(0L);
        when(prestamoRepository.countByEstadoAndFechaLimiteBefore(
                any(EstadoPrestamo.class), any(LocalDate.class))).thenReturn(0L);
        when(multaRepository.countByEstadoNot(any(EstadoMulta.class))).thenReturn(0L);
        when(multaRepository.sumarImporteTotalByEstadoNot(any(EstadoMulta.class)))
                .thenReturn(BigDecimal.ZERO);
        when(pagoMultaRepository.sumarPagosByMultaEstadoNot(any(EstadoMulta.class)))
                .thenReturn(BigDecimal.ZERO);

        DashboardStats stats = dashboardService.obtenerEstadisticas();

        assertEquals(0, stats.getTotalEjemplares());
        assertEquals(0, stats.getEjemplaresDisponibles());
        assertEquals(0, stats.getEjemplaresInactivos());
        assertEquals(0, stats.getPrestamosActivos());
        assertEquals(0, stats.getPrestamosVencidos());
        assertEquals(0, stats.getPrestamosDevueltos());
        assertEquals(0, stats.getMultasPendientes());
        assertEquals(BigDecimal.ZERO, stats.getSaldoTotalPendiente());
    }

    @Test
    void calculaTodosLosIndicadoresConDatosReales() {
        when(libroRepository.count()).thenReturn(50L);
        when(libroRepository.countByActivoTrue()).thenReturn(45L);
        when(libroRepository.countByActivoFalse()).thenReturn(5L);
        when(prestamoRepository.countByEstado(eq(EstadoPrestamo.ACTIVO))).thenReturn(12L);
        when(prestamoRepository.countByEstadoAndFechaLimiteBefore(
                eq(EstadoPrestamo.ACTIVO), any(LocalDate.class))).thenReturn(3L);
        when(prestamoRepository.countByEstado(eq(EstadoPrestamo.DEVUELTO))).thenReturn(20L);
        when(multaRepository.countByEstadoNot(eq(EstadoMulta.PAGADA))).thenReturn(7L);
        when(multaRepository.sumarImporteTotalByEstadoNot(eq(EstadoMulta.PAGADA)))
                .thenReturn(new BigDecimal("15000.00"));
        when(pagoMultaRepository.sumarPagosByMultaEstadoNot(eq(EstadoMulta.PAGADA)))
                .thenReturn(new BigDecimal("5000.00"));

        DashboardStats stats = dashboardService.obtenerEstadisticas();

        assertEquals(50, stats.getTotalEjemplares());
        assertEquals(5, stats.getEjemplaresInactivos());
        assertEquals(33, stats.getEjemplaresDisponibles());
        assertEquals(12, stats.getPrestamosActivos());
        assertEquals(3, stats.getPrestamosVencidos());
        assertEquals(20, stats.getPrestamosDevueltos());
        assertEquals(7, stats.getMultasPendientes());
        assertEquals(new BigDecimal("10000.00"), stats.getSaldoTotalPendiente());
    }

    @Test
    void prestamoVencidoSeCuentaCorrectamente() {
        when(libroRepository.count()).thenReturn(10L);
        when(libroRepository.countByActivoTrue()).thenReturn(10L);
        when(libroRepository.countByActivoFalse()).thenReturn(0L);
        when(prestamoRepository.countByEstado(eq(EstadoPrestamo.ACTIVO))).thenReturn(5L);
        when(prestamoRepository.countByEstadoAndFechaLimiteBefore(
                eq(EstadoPrestamo.ACTIVO), any(LocalDate.class))).thenReturn(2L);
        when(prestamoRepository.countByEstado(eq(EstadoPrestamo.DEVUELTO))).thenReturn(0L);
        when(multaRepository.countByEstadoNot(any(EstadoMulta.class))).thenReturn(0L);
        when(multaRepository.sumarImporteTotalByEstadoNot(any(EstadoMulta.class)))
                .thenReturn(BigDecimal.ZERO);
        when(pagoMultaRepository.sumarPagosByMultaEstadoNot(any(EstadoMulta.class)))
                .thenReturn(BigDecimal.ZERO);

        DashboardStats stats = dashboardService.obtenerEstadisticas();

        assertEquals(2, stats.getPrestamosVencidos());
        assertEquals(5, stats.getPrestamosActivos());
        assertEquals(5, stats.getEjemplaresDisponibles());
    }

    @Test
    void saldoPendienteConPagosParciales() {
        when(libroRepository.count()).thenReturn(10L);
        when(libroRepository.countByActivoTrue()).thenReturn(10L);
        when(libroRepository.countByActivoFalse()).thenReturn(0L);
        when(prestamoRepository.countByEstado(any(EstadoPrestamo.class))).thenReturn(0L);
        when(prestamoRepository.countByEstadoAndFechaLimiteBefore(
                any(EstadoPrestamo.class), any(LocalDate.class))).thenReturn(0L);
        when(multaRepository.countByEstadoNot(eq(EstadoMulta.PAGADA))).thenReturn(3L);
        when(multaRepository.sumarImporteTotalByEstadoNot(eq(EstadoMulta.PAGADA)))
                .thenReturn(new BigDecimal("8000.00"));
        when(pagoMultaRepository.sumarPagosByMultaEstadoNot(eq(EstadoMulta.PAGADA)))
                .thenReturn(new BigDecimal("3000.00"));

        DashboardStats stats = dashboardService.obtenerEstadisticas();

        assertEquals(new BigDecimal("5000.00"), stats.getSaldoTotalPendiente());
        assertEquals(3, stats.getMultasPendientes());
    }

    @Test
    void ejemplaresDisponiblesNoSeHacenNegativos() {
        when(libroRepository.count()).thenReturn(5L);
        when(libroRepository.countByActivoTrue()).thenReturn(3L);
        when(libroRepository.countByActivoFalse()).thenReturn(2L);
        when(prestamoRepository.countByEstado(eq(EstadoPrestamo.ACTIVO))).thenReturn(2L);
        when(prestamoRepository.countByEstadoAndFechaLimiteBefore(
                eq(EstadoPrestamo.ACTIVO), any(LocalDate.class))).thenReturn(1L);
        when(prestamoRepository.countByEstado(eq(EstadoPrestamo.DEVUELTO))).thenReturn(0L);
        when(multaRepository.countByEstadoNot(any(EstadoMulta.class))).thenReturn(0L);
        when(multaRepository.sumarImporteTotalByEstadoNot(any(EstadoMulta.class)))
                .thenReturn(BigDecimal.ZERO);
        when(pagoMultaRepository.sumarPagosByMultaEstadoNot(any(EstadoMulta.class)))
                .thenReturn(BigDecimal.ZERO);

        DashboardStats stats = dashboardService.obtenerEstadisticas();

        assertEquals(1, stats.getEjemplaresDisponibles());
    }

    @Test
    void multasPagadasNoSeCuentanComoPendientes() {
        when(libroRepository.count()).thenReturn(10L);
        when(libroRepository.countByActivoTrue()).thenReturn(10L);
        when(libroRepository.countByActivoFalse()).thenReturn(0L);
        when(prestamoRepository.countByEstado(any(EstadoPrestamo.class))).thenReturn(0L);
        when(prestamoRepository.countByEstadoAndFechaLimiteBefore(
                any(EstadoPrestamo.class), any(LocalDate.class))).thenReturn(0L);
        when(multaRepository.countByEstadoNot(any(EstadoMulta.class))).thenReturn(0L);
        when(multaRepository.sumarImporteTotalByEstadoNot(any(EstadoMulta.class)))
                .thenReturn(BigDecimal.ZERO);
        when(pagoMultaRepository.sumarPagosByMultaEstadoNot(any(EstadoMulta.class)))
                .thenReturn(BigDecimal.ZERO);

        DashboardStats stats = dashboardService.obtenerEstadisticas();

        assertEquals(0, stats.getMultasPendientes());
        assertEquals(BigDecimal.ZERO, stats.getSaldoTotalPendiente());
    }
}
