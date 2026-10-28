package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.dto.LibroForm;
import com.ingsoft.biblioteca.entity.Categoria;
import com.ingsoft.biblioteca.entity.EstadoPrestamo;
import com.ingsoft.biblioteca.entity.Libro;
import com.ingsoft.biblioteca.repository.CategoriaRepository;
import com.ingsoft.biblioteca.repository.LibroRepository;
import com.ingsoft.biblioteca.repository.PrestamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    @Mock private LibroRepository libroRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private PrestamoRepository prestamoRepository;

    private LibroService libroService;

    @BeforeEach
    void setUp() {
        libroService = new LibroService(
                libroRepository, categoriaRepository, prestamoRepository
        );
    }

    @Test
    void creaEjemplarActivoConCategoriaExistente() {
        Categoria categoria = categoria(4L, "Historia");
        when(categoriaRepository.findById(4L)).thenReturn(Optional.of(categoria));
        when(libroRepository.save(any(Libro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Libro libro = libroService.crear(formulario(4L));

        assertTrue(libro.isActivo());
        assertEquals("Título de prueba", libro.getTitulo());
        assertEquals("Autora de prueba", libro.getAutor());
        assertSame(categoria, libro.getCategoria());
    }

    @Test
    void actualizaDatosSinCambiarElEstadoDelEjemplar() {
        Libro libro = new Libro();
        libro.setActivo(false);
        libro.setCategoria(categoria(1L, "Anterior"));
        when(libroRepository.buscarPorIdConCategoria(7L)).thenReturn(Optional.of(libro));
        when(categoriaRepository.findById(4L)).thenReturn(Optional.of(categoria(4L, "Historia")));
        when(libroRepository.save(any(Libro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Libro actualizado = libroService.actualizar(7L, formulario(4L));

        assertFalse(actualizado.isActivo());
        assertEquals("Historia", actualizado.getCategoria().getNombre());
    }

    @Test
    void disponibilidadRequiereEjemplarActivoYSinPrestamoActivo() {
        Libro disponible = libro(1L, true);
        Libro prestado = libro(2L, true);
        Libro inactivo = libro(3L, false);
        when(prestamoRepository.existsByLibroIdAndEstado(1L, EstadoPrestamo.ACTIVO)).thenReturn(false);
        when(prestamoRepository.existsByLibroIdAndEstado(2L, EstadoPrestamo.ACTIVO)).thenReturn(true);
        when(prestamoRepository.existsByLibroIdAndEstado(3L, EstadoPrestamo.ACTIVO)).thenReturn(false);

        Map<Long, Boolean> disponibilidad = libroService.calcularDisponibilidades(
                List.of(disponible, prestado, inactivo)
        );

        assertTrue(disponibilidad.get(1L));
        assertFalse(disponibilidad.get(2L));
        assertFalse(disponibilidad.get(3L));
    }

    @Test
    void rechazaCategoriaInexistente() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> libroService.crear(formulario(99L)));
        verify(libroRepository, never()).save(any());
    }

    private LibroForm formulario(Long categoriaId) {
        LibroForm formulario = new LibroForm();
        formulario.setTitulo("  Título de prueba  ");
        formulario.setAutor("  Autora de prueba  ");
        formulario.setCategoriaId(categoriaId);
        return formulario;
    }

    private Categoria categoria(Long id, String nombre) {
        Categoria categoria = new Categoria();
        org.springframework.test.util.ReflectionTestUtils.setField(categoria, "id", id);
        categoria.setNombre(nombre);
        return categoria;
    }

    private Libro libro(Long id, boolean activo) {
        Libro libro = new Libro();
        org.springframework.test.util.ReflectionTestUtils.setField(libro, "id", id);
        libro.setActivo(activo);
        return libro;
    }
}
