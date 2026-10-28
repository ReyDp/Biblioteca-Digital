package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.entity.*;
import com.ingsoft.biblioteca.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private MultaService multaService;

    private PrestamoService prestamoService;

    @BeforeEach
    void setUp() {
        prestamoService = new PrestamoService(
                prestamoRepository,
                clienteRepository,
                libroRepository,
                multaService
        );
    }

    @Test
    void registraPrestamoConPlazoDeQuinceDias() {
        Cliente cliente = new Cliente();

        Libro libro = new Libro();
        libro.setActivo(true);

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(libroRepository.buscarPorIdConBloqueo(2L))
                .thenReturn(Optional.of(libro));

        when(prestamoRepository.existsByLibroIdAndEstado(
                2L, EstadoPrestamo.ACTIVO))
                .thenReturn(false);

        when(prestamoRepository.save(any(Prestamo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate hoy = LocalDate.now();

        Prestamo prestamo = prestamoService.registrarPrestamo(1L, 2L);

        assertEquals(hoy, prestamo.getFechaPrestamo());
        assertEquals(hoy.plusDays(15), prestamo.getFechaLimite());
        assertEquals(EstadoPrestamo.ACTIVO, prestamo.getEstado());
    }

    @Test
    void rechazaLibroQueYaTienePrestamoActivo() {
        Cliente cliente = new Cliente();

        Libro libro = new Libro();
        libro.setActivo(true);

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(libroRepository.buscarPorIdConBloqueo(2L))
                .thenReturn(Optional.of(libro));

        when(prestamoRepository.existsByLibroIdAndEstado(
                2L, EstadoPrestamo.ACTIVO))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> prestamoService.registrarPrestamo(1L, 2L)
        );

        verify(prestamoRepository, never())
                .save(any(Prestamo.class));
    }

    @Test
    void rechazaLibroInactivo() {
        Cliente cliente = new Cliente();

        Libro libro = new Libro();
        libro.setActivo(false);

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(libroRepository.buscarPorIdConBloqueo(2L))
                .thenReturn(Optional.of(libro));

        assertThrows(
                IllegalStateException.class,
                () -> prestamoService.registrarPrestamo(1L, 2L)
        );
    }


    @Test
    void registraDevolucionYGeneraMultaDefinitiva() {
        Prestamo prestamo = new Prestamo();
        prestamo.setFechaLimite(LocalDate.now().minusDays(3));
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        when(prestamoRepository.buscarPorIdConBloqueo(1L))
                .thenReturn(Optional.of(prestamo));

        when(prestamoRepository.save(any(Prestamo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        prestamoService.registrarDevolucion(1L);

        assertEquals(EstadoPrestamo.DEVUELTO, prestamo.getEstado());
        assertEquals(LocalDate.now(), prestamo.getFechaDevolucion());

        verify(prestamoRepository).save(prestamo);
        verify(multaService).registrarMultaDefinitiva(
                prestamo, prestamo.getFechaDevolucion()
        );
    }

    @Test
    void rechazaDevolverUnPrestamoYaDevuelto() {
        Prestamo prestamo = new Prestamo();
        prestamo.setFechaLimite(LocalDate.now().minusDays(3));
        prestamo.setFechaDevolucion(LocalDate.now().minusDays(1));
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);

        when(prestamoRepository.buscarPorIdConBloqueo(1L))
                .thenReturn(Optional.of(prestamo));

        assertThrows(
                IllegalStateException.class,
                () -> prestamoService.registrarDevolucion(1L)
        );

        verify(prestamoRepository, never()).save(any(Prestamo.class));
        verify(multaService, never())
                .registrarMultaDefinitiva(any(Prestamo.class), any(LocalDate.class));
    }


    @Test
    void rechazaDevolverPrestamoInexistente() {
        when(prestamoRepository.buscarPorIdConBloqueo(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> prestamoService.registrarDevolucion(99L)
        );

        verify(prestamoRepository, never())
                .save(any(Prestamo.class));

        verify(multaService, never())
                .registrarMultaDefinitiva(
                        any(Prestamo.class),
                        any(LocalDate.class)
                );
    }

    @Test
    void devolucionEnFechaLimiteSolicitaCalculoDeMulta() {
        Prestamo prestamo = new Prestamo();
        prestamo.setFechaLimite(LocalDate.now());
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        when(prestamoRepository.buscarPorIdConBloqueo(1L))
                .thenReturn(Optional.of(prestamo));

        when(prestamoRepository.save(any(Prestamo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Prestamo resultado = prestamoService.registrarDevolucion(1L);

        assertEquals(EstadoPrestamo.DEVUELTO, resultado.getEstado());
        assertEquals(LocalDate.now(), resultado.getFechaDevolucion());

        verify(multaService).registrarMultaDefinitiva(
                prestamo,
                LocalDate.now()
        );
    }

    @Test
    void listaSoloLosPrestamosDelClienteSolicitado() {
        Prestamo prestamo = new Prestamo();
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(prestamoRepository.buscarPorClienteIdConDetalles(1L))
                .thenReturn(java.util.List.of(prestamo));

        assertEquals(
                java.util.List.of(prestamo),
                prestamoService.listarPrestamosDeCliente(1L)
        );

        verify(prestamoRepository).buscarPorClienteIdConDetalles(1L);
    }

    @Test
    void rechazaConsultarPrestamoDeOtroCliente() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(prestamoRepository.buscarPorIdYClienteIdConDetalles(9L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> prestamoService.obtenerPrestamoDeCliente(1L, 9L)
        );
    }

    @Test
    void listaPrestamosActivosParaAdministracion() {
        Prestamo prestamo = new Prestamo();
        when(prestamoRepository.buscarPorEstadoConDetalles(
                EstadoPrestamo.ACTIVO
        )).thenReturn(java.util.List.of(prestamo));

        assertEquals(
                java.util.List.of(prestamo),
                prestamoService.listarPrestamosActivos()
        );
    }


}
