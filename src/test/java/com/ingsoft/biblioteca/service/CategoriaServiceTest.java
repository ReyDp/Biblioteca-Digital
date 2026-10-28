package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.dto.CategoriaForm;
import com.ingsoft.biblioteca.entity.Categoria;
import com.ingsoft.biblioteca.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock private CategoriaRepository categoriaRepository;
    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService(categoriaRepository);
    }

    @Test
    void creaCategoriaConNombreNormalizado() {
        when(categoriaRepository.existsByNombreIgnoreCase("Historia")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Categoria categoria = categoriaService.crear(formulario("  Historia  "));

        assertEquals("Historia", categoria.getNombre());
    }

    @Test
    void rechazaNombreDuplicadoAlCrear() {
        when(categoriaRepository.existsByNombreIgnoreCase("Historia")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoriaService.crear(formulario("Historia")));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void permiteMantenerElMismoNombreAlEditar() {
        Categoria categoria = categoria(2L, "Historia");
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.findByNombreIgnoreCase("Historia")).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Categoria actualizada = categoriaService.actualizar(2L, formulario("Historia"));

        assertEquals("Historia", actualizada.getNombre());
    }

    @Test
    void rechazaNombreDeOtraCategoriaAlEditar() {
        Categoria actual = categoria(2L, "Historia");
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(actual));
        when(categoriaRepository.findByNombreIgnoreCase("Ciencia"))
                .thenReturn(Optional.of(categoria(3L, "Ciencia")));

        assertThrows(IllegalArgumentException.class,
                () -> categoriaService.actualizar(2L, formulario("Ciencia")));
        verify(categoriaRepository, never()).save(any());
    }

    private CategoriaForm formulario(String nombre) {
        CategoriaForm formulario = new CategoriaForm();
        formulario.setNombre(nombre);
        formulario.setDescripcion("Descripción");
        return formulario;
    }

    private Categoria categoria(Long id, String nombre) {
        Categoria categoria = new Categoria();
        org.springframework.test.util.ReflectionTestUtils.setField(categoria, "id", id);
        categoria.setNombre(nombre);
        return categoria;
    }
}
