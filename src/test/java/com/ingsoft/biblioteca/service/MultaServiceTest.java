package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.entity.*;
import com.ingsoft.biblioteca.repository.MultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultaServiceTest {

    @Mock
    private MultaRepository multaRepository;

    private MultaService multaService;

    @BeforeEach
    void setUp() {
        multaService = new MultaService(multaRepository);
    }


    @Test
    void devolucionAntesDeFechaLimiteNoGeneraMulta() {
        LocalDate fechaLimite = LocalDate.of(2026, 11, 16);

        BigDecimal multa = multaService.calcularMultaEstimada(
                crearPrestamo(fechaLimite),
                fechaLimite.minusDays(5)
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(multa));
    }

    @Test
    void fechasNulasSonRechazadas() {
        assertThrows(
                IllegalArgumentException.class,
                () -> multaService.calcularDiasRetraso(
                        null,
                        LocalDate.of(2026, 11, 16)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> multaService.calcularDiasRetraso(
                        LocalDate.of(2026, 11, 16),
                        null
                )
        );
    }

    @Test
    void devolucionEnFechaLimiteNoGeneraMulta() {
        LocalDate fechaLimite = LocalDate.of(2026, 11, 16);

        BigDecimal multa = multaService.calcularMultaEstimada(
                crearPrestamo(fechaLimite),
                fechaLimite
        );

        assertEquals(new BigDecimal("0.00"), multa);
    }

    @Test
    void unDiaDeRetrasoGeneraMultaDeMilPesos() {
        LocalDate fechaLimite = LocalDate.of(2026, 11, 16);

        BigDecimal multa = multaService.calcularMultaEstimada(
                crearPrestamo(fechaLimite),
                fechaLimite.plusDays(1)
        );

        assertEquals(new BigDecimal("1000.00"), multa);
    }

    @Test
    void cincoDiasDeRetrasoGeneranMultaDeCincoMilPesos() {
        LocalDate fechaLimite = LocalDate.of(2026, 11, 16);

        BigDecimal multa = multaService.calcularMultaEstimada(
                crearPrestamo(fechaLimite),
                fechaLimite.plusDays(5)
        );

        assertEquals(new BigDecimal("5000.00"), multa);
    }

    @Test
    void multaEstimadaAumentaConCadaDiaDeRetraso() {
        LocalDate fechaLimite = LocalDate.of(2026, 11, 16);
        Prestamo prestamo = crearPrestamo(fechaLimite);

        BigDecimal diaUno = multaService.calcularMultaEstimada(
                prestamo, fechaLimite.plusDays(1));

        BigDecimal diaDos = multaService.calcularMultaEstimada(
                prestamo, fechaLimite.plusDays(2));

        assertEquals(
                0,
                diaDos.compareTo(diaUno.add(new BigDecimal("1000.00")))
        );
    }

    @Test
    void registrarMultaDefinitivaGuardaElValorCorrecto() {
        LocalDate fechaLimite = LocalDate.of(2026, 11, 16);
        LocalDate devolucion = fechaLimite.plusDays(3);

        Prestamo prestamo = crearPrestamo(fechaLimite);

        // Simular un préstamo persistido.
        org.springframework.test.util.ReflectionTestUtils
                .setField(prestamo, "id", 10L);

        when(multaRepository.findByPrestamoId(10L))
                .thenReturn(Optional.empty());

        when(multaRepository.save(any(Multa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Multa multa = multaService.registrarMultaDefinitiva(
                prestamo,
                devolucion
        );

        assertEquals(3, multa.getDiasRetrasoFinales());

        assertEquals(
                0,
                new BigDecimal("3000.00")
                        .compareTo(multa.getImporteTotal())
        );

        assertEquals(
                new BigDecimal("1000.00"),
                multa.getTarifaDiaria()
        );

        assertEquals(devolucion, multa.getFechaCalculo());
        assertEquals(EstadoMulta.PENDIENTE, multa.getEstado());

        verify(multaRepository).findByPrestamoId(10L);
        verify(multaRepository).save(any(Multa.class));
    }


    @Test
    void registrarMultaDefinitivaNoDuplicaUnaMultaExistente() {
        LocalDate fechaLimite = LocalDate.of(2026, 11, 16);
        LocalDate devolucion = fechaLimite.plusDays(3);

        Prestamo prestamo = crearPrestamo(fechaLimite);

        // Simular que el préstamo ya está persistido.
        org.springframework.test.util.ReflectionTestUtils
                .setField(prestamo, "id", 10L);

        // Simular una multa que ya existe para ese préstamo.
        Multa multaExistente = new Multa();
        multaExistente.setPrestamo(prestamo);
        multaExistente.setTarifaDiaria(new BigDecimal("1000.00"));
        multaExistente.setDiasRetrasoFinales(3);
        multaExistente.setImporteTotal(new BigDecimal("3000.00"));
        multaExistente.setFechaCalculo(devolucion);
        multaExistente.setEstado(EstadoMulta.PENDIENTE);

        when(multaRepository.findByPrestamoId(10L))
                .thenReturn(Optional.of(multaExistente));

        // Ejecutar el registro cuando ya existe una multa.
        Multa resultado = multaService.registrarMultaDefinitiva(
                prestamo,
                devolucion
        );

        // Debe devolver la multa existente.
        assertSame(multaExistente, resultado);

        // No debe guardar una segunda multa.
        verify(multaRepository).findByPrestamoId(10L);
        verify(multaRepository, never()).save(any(Multa.class));
    }


    @Test
    void obtenerMultaPorPrestamoIdDevuelveLaMultaExistente() {
        Multa multa = new Multa();
        org.springframework.test.util.ReflectionTestUtils.setField(multa, "id", 1L);
        multa.setImporteTotal(new BigDecimal("3000.00"));
        multa.setDiasRetrasoFinales(3);
        multa.setEstado(EstadoMulta.PENDIENTE);

        when(multaRepository.findByPrestamoId(10L))
                .thenReturn(Optional.of(multa));

        Optional<Multa> resultado = multaService.obtenerMultaPorPrestamoId(10L);

        assertTrue(resultado.isPresent());
        assertSame(multa, resultado.get());
        assertEquals(new BigDecimal("3000.00"), resultado.get().getImporteTotal());
        assertEquals(EstadoMulta.PENDIENTE, resultado.get().getEstado());
        verify(multaRepository).findByPrestamoId(10L);
    }

    @Test
    void obtenerMultaPorPrestamoIdDevuelveVacioCuandoNoExiste() {
        when(multaRepository.findByPrestamoId(99L))
                .thenReturn(Optional.empty());

        Optional<Multa> resultado = multaService.obtenerMultaPorPrestamoId(99L);

        assertTrue(resultado.isEmpty());
        verify(multaRepository).findByPrestamoId(99L);
    }

    @Test
    void rechazaConsultarMultaDeOtroCliente() {
        when(multaRepository.buscarPorIdYClienteIdConDetalles(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> multaService.obtenerMultaDeCliente(1L, 10L)
        );

        verify(multaRepository).buscarPorIdYClienteIdConDetalles(10L, 1L);
    }

    @Test
    void devuelveLaMultaCuandoPerteneceAlCliente() {
        Prestamo prestamo = crearPrestamo(LocalDate.of(2026, 11, 16));
        Multa multa = new Multa();
        org.springframework.test.util.ReflectionTestUtils.setField(multa, "id", 10L);
        multa.setPrestamo(prestamo);
        multa.setImporteTotal(new BigDecimal("3000.00"));
        multa.setEstado(EstadoMulta.PENDIENTE);

        when(multaRepository.buscarPorIdYClienteIdConDetalles(10L, 1L))
                .thenReturn(Optional.of(multa));

        Multa resultado = multaService.obtenerMultaDeCliente(1L, 10L);

        assertSame(multa, resultado);
        assertEquals(new BigDecimal("3000.00"), resultado.getImporteTotal());
        verify(multaRepository).buscarPorIdYClienteIdConDetalles(10L, 1L);
    }

    private Prestamo crearPrestamo(LocalDate fechaLimite) {
        Prestamo prestamo = new Prestamo();
        prestamo.setFechaLimite(fechaLimite);
        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        return prestamo;
    }

}