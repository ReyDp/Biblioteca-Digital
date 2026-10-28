package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.dto.LibroForm;
import com.ingsoft.biblioteca.entity.Libro;
import com.ingsoft.biblioteca.service.CategoriaService;
import com.ingsoft.biblioteca.service.LibroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/libros")
public class AdminLibroController {

    private final LibroService libroService;
    private final CategoriaService categoriaService;

    public AdminLibroController(
            LibroService libroService,
            CategoriaService categoriaService
    ) {
        this.libroService = libroService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String texto,
            Model model
    ) {
        var libros = libroService.buscarParaAdministracion(texto);
        model.addAttribute("libros", libros);
        model.addAttribute("disponibilidad", libroService.calcularDisponibilidades(libros));
        model.addAttribute("texto", texto == null ? "" : texto);
        return "admin-libros";
    }

    @GetMapping("/nuevo")
    public String mostrarCreacion(Model model) {
        prepararFormulario(model, new LibroForm());
        return "admin-libro-form";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("libro") LibroForm formulario,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (resultado.hasErrors()) {
            prepararFormulario(model, formulario);
            return "admin-libro-form";
        }

        try {
            libroService.crear(formulario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Libro registrado correctamente.");
            return "redirect:/admin/libros";
        } catch (IllegalArgumentException e) {
            resultado.reject("libro", e.getMessage());
            prepararFormulario(model, formulario);
            return "admin-libro-form";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarEdicion(@PathVariable Long id, Model model) {
        Libro libro = obtenerO404(id);
        prepararFormulario(model, crearFormulario(libro));
        model.addAttribute("libroId", id);
        return "admin-libro-form";
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("libro") LibroForm formulario,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (resultado.hasErrors()) {
            prepararFormulario(model, formulario);
            model.addAttribute("libroId", id);
            return "admin-libro-form";
        }

        try {
            libroService.actualizar(id, formulario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Libro actualizado correctamente.");
            return "redirect:/admin/libros";
        } catch (IllegalArgumentException e) {
            resultado.reject("libro", e.getMessage());
            prepararFormulario(model, formulario);
            model.addAttribute("libroId", id);
            return "admin-libro-form";
        }
    }

    private void prepararFormulario(Model model, LibroForm formulario) {
        model.addAttribute("libro", formulario);
        model.addAttribute("categorias", categoriaService.listar());
    }

    private LibroForm crearFormulario(Libro libro) {
        LibroForm formulario = new LibroForm();
        formulario.setTitulo(libro.getTitulo());
        formulario.setAutor(libro.getAutor());
        formulario.setDescripcion(libro.getDescripcion());
        formulario.setPortadaUrl(libro.getPortadaUrl());
        formulario.setArchivoUrl(libro.getArchivoUrl());
        formulario.setCategoriaId(libro.getCategoria().getId());
        return formulario;
    }

    private Libro obtenerO404(Long id) {
        try {
            return libroService.obtener(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
