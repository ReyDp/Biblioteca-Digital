package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.entity.EstadoPrestamo;
import com.ingsoft.biblioteca.entity.Libro;
import com.ingsoft.biblioteca.repository.CategoriaRepository;
import com.ingsoft.biblioteca.repository.LibroRepository;
import com.ingsoft.biblioteca.repository.PrestamoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class CatalogoController {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final PrestamoRepository prestamoRepository;

    public CatalogoController(
            LibroRepository libroRepository,
            CategoriaRepository categoriaRepository,
            PrestamoRepository prestamoRepository
    ) {
        this.libroRepository = libroRepository;
        this.categoriaRepository = categoriaRepository;
        this.prestamoRepository = prestamoRepository;
    }


    @GetMapping({"/", "/catalogo"})
    public String catalogo(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long categoriaId,
            Model model
    ) {
        String busqueda = (texto == null) ? "" : texto.trim();

        Long categoriaFiltro = (categoriaId == null) ? 0L : categoriaId;

        var libros = libroRepository.buscarCatalogo(
                busqueda,
                categoriaFiltro
        );

        model.addAttribute("libros", libros);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("texto", texto == null ? "" : texto);
        model.addAttribute("categoriaSeleccionada", categoriaId);

        java.util.Map<Long, Boolean> disponibilidad =
                new java.util.HashMap<>();

        for (Libro libro : libros) {
            disponibilidad.put(
                    libro.getId(),
                    !prestamoRepository.existsByLibroIdAndEstado(
                            libro.getId(),
                            EstadoPrestamo.ACTIVO
                    )
            );
        }

        model.addAttribute("disponibilidad", disponibilidad);

        return "catalogo";
    }

    @Transactional(readOnly = true)
    @GetMapping("/libros/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model
    ) {
        Libro libro = libroRepository.buscarPorIdConCategoria(id)
                .filter(Libro::isActivo)
                .orElseThrow(() ->
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "Libro no encontrado"
                        )
                );

        boolean disponible = !prestamoRepository.existsByLibroIdAndEstado(
                id, EstadoPrestamo.ACTIVO
        );

        model.addAttribute("libro", libro);
        model.addAttribute("disponible", disponible);

        return "detalle-libro";
    }
}