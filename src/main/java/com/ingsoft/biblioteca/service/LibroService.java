package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.dto.LibroForm;
import com.ingsoft.biblioteca.entity.Categoria;
import com.ingsoft.biblioteca.entity.EstadoPrestamo;
import com.ingsoft.biblioteca.entity.Libro;
import com.ingsoft.biblioteca.repository.CategoriaRepository;
import com.ingsoft.biblioteca.repository.LibroRepository;
import com.ingsoft.biblioteca.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final PrestamoRepository prestamoRepository;

    public LibroService(
            LibroRepository libroRepository,
            CategoriaRepository categoriaRepository,
            PrestamoRepository prestamoRepository
    ) {
        this.libroRepository = libroRepository;
        this.categoriaRepository = categoriaRepository;
        this.prestamoRepository = prestamoRepository;
    }

    @Transactional(readOnly = true)
    public List<Libro> buscarParaAdministracion(String texto) {
        return libroRepository.buscarParaAdministracion(limpiar(texto));
    }

    @Transactional(readOnly = true)
    public Libro obtener(Long id) {
        return libroRepository.buscarPorIdConCategoria(id)
                .orElseThrow(() -> new IllegalArgumentException("El libro no existe."));
    }

    @Transactional(readOnly = true)
    public Map<Long, Boolean> calcularDisponibilidades(List<Libro> libros) {
        Map<Long, Boolean> disponibilidades = new LinkedHashMap<>();
        for (Libro libro : libros) {
            boolean prestamoActivo = prestamoRepository.existsByLibroIdAndEstado(
                    libro.getId(),
                    EstadoPrestamo.ACTIVO
            );
            disponibilidades.put(libro.getId(), libro.isActivo() && !prestamoActivo);
        }
        return disponibilidades;
    }

    @Transactional
    public Libro crear(LibroForm formulario) {
        Libro libro = new Libro();
        aplicarFormulario(libro, formulario);
        libro.setActivo(true);
        return libroRepository.save(libro);
    }

    @Transactional
    public Libro actualizar(Long id, LibroForm formulario) {
        Libro libro = obtener(id);
        aplicarFormulario(libro, formulario);
        return libroRepository.save(libro);
    }

    private void aplicarFormulario(Libro libro, LibroForm formulario) {
        Categoria categoria = categoriaRepository.findById(formulario.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("La categoría seleccionada no existe."));

        libro.setTitulo(limpiar(formulario.getTitulo()));
        libro.setAutor(limpiar(formulario.getAutor()));
        libro.setDescripcion(limpiarOpcional(formulario.getDescripcion()));
        libro.setPortadaUrl(limpiarOpcional(formulario.getPortadaUrl()));
        libro.setArchivoUrl(limpiarOpcional(formulario.getArchivoUrl()));
        libro.setCategoria(categoria);
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String limpiarOpcional(String valor) {
        String resultado = limpiar(valor);
        return resultado.isEmpty() ? null : resultado;
    }
}
