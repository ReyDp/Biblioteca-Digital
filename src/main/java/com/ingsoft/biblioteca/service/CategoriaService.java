package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.dto.CategoriaForm;
import com.ingsoft.biblioteca.entity.Categoria;
import com.ingsoft.biblioteca.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Categoria obtener(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La categoría no existe."));
    }

    @Transactional
    public Categoria crear(CategoriaForm formulario) {
        String nombre = limpiar(formulario.getNombre());
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(limpiarOpcional(formulario.getDescripcion()));
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizar(Long id, CategoriaForm formulario) {
        Categoria categoria = obtener(id);
        String nombre = limpiar(formulario.getNombre());

        categoriaRepository.findByNombreIgnoreCase(nombre)
                .filter(existente -> !existente.getId().equals(categoria.getId()))
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
                });

        categoria.setNombre(nombre);
        categoria.setDescripcion(limpiarOpcional(formulario.getDescripcion()));
        return categoriaRepository.save(categoria);
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String limpiarOpcional(String valor) {
        String resultado = limpiar(valor);
        return resultado.isEmpty() ? null : resultado;
    }
}
