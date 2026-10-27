package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.entity.*;
import com.ingsoft.biblioteca.repository.MultaRepository;
import com.ingsoft.biblioteca.repository.PagoMultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoMultaServiceTest {

    @Mock
    private MultaRepository multaRepository;

    @Mock
    private PagoMultaRepository pagoMultaRepository;

    private PagoMultaService pagoService;

    private Multa multa;

    @BeforeEach
    void setUp() {
        pagoService = new PagoMultaService(
                multaRepository,
                pagoMultaRepository
        );

        multa = new Multa();
        multa.setImporteTotal(new BigDecimal("5000.00"));
        multa.setEstado(EstadoMulta.PENDIENTE);
    }

    @Test
    void pagoParcialActualizaEstadoDeMulta() {
        when(multaRepository.buscarPorIdConBloqueo(1L))
                .thenReturn(Optional.of(multa));

        when(pagoMultaRepository.sumarPagosPorMulta(1L))
                .thenReturn(BigDecimal.ZERO);

        when(pagoMultaRepository.save(any(PagoMulta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PagoMulta pago = pagoService.registrarPago(
                1L,
                new BigDecimal("2000.00"),
                "EFECTIVO",
                null
        );

        assertEquals(
                0,
                new BigDecimal("2000.00")
                        .compareTo(pago.getValorPagado())
        );

        assertEquals(EstadoMulta.PARCIAL, multa.getEstado());

        verify(multaRepository).save(multa);
    }

    @Test
    void pagoCompletoMarcaMultaComoPagada() {
        when(multaRepository.buscarPorIdConBloqueo(1L))
                .thenReturn(Optional.of(multa));

        when(pagoMultaRepository.sumarPagosPorMulta(1L))
                .thenReturn(BigDecimal.ZERO);

        when(pagoMultaRepository.save(any(PagoMulta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        pagoService.registrarPago(
                1L,
                new BigDecimal("5000.00"),
                "TRANSFERENCIA",
                "REF-001"
        );

        assertEquals(EstadoMulta.PAGADA, multa.getEstado());
    }

    @Test
    void rechazaPagoSuperiorAlSaldoPendiente() {
        when(multaRepository.buscarPorIdConBloqueo(1L))
                .thenReturn(Optional.of(multa));

        when(pagoMultaRepository.sumarPagosPorMulta(1L))
                .thenReturn(new BigDecimal("3000.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> pagoService.registrarPago(
                        1L,
                        new BigDecimal("3000.00"),
                        "EFECTIVO",
                        null
                )
        );

        verify(pagoMultaRepository, never())
                .save(any(PagoMulta.class));
    }

    @Test
    void rechazaPagoCero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pagoService.registrarPago(
                        1L,
                        BigDecimal.ZERO,
                        "EFECTIVO",
                        null
                )
        );

        verify(multaRepository, never())
                .buscarPorIdConBloqueo(anyLong());
    }
}