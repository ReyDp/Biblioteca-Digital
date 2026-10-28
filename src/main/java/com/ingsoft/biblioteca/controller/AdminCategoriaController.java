package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.dto.CategoriaForm;
import com.ingsoft.biblioteca.entity.Categoria;
import com.ingsoft.biblioteca.service.CategoriaService;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
public class AdminCategoriaController {

    private final CategoriaService categoriaService;

    public AdminCategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listar());
        return "admin-categorias";
    }

    @GetMapping("/nueva")
    public String mostrarCreacion(Model model) {
        model.addAttribute("categoria", new CategoriaForm());
        return "admin-categoria-form";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("categoria") CategoriaForm formulario,
            BindingResult resultado,
            RedirectAttributes redirectAttributes
    ) {
        if (resultado.hasErrors()) {
            return "admin-categoria-form";
        }

        try {
            categoriaService.crear(formulario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Categoría registrada correctamente.");
            return "redirect:/admin/categorias";
        } catch (IllegalArgumentException e) {
            resultado.rejectValue("nombre", "categoria.duplicada", e.getMessage());
            return "admin-categoria-form";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarEdicion(@PathVariable Long id, Model model) {
        Categoria categoria = obtenerO404(id);
        CategoriaForm formulario = new CategoriaForm();
        formulario.setNombre(categoria.getNombre());
        formulario.setDescripcion(categoria.getDescripcion());
        model.addAttribute("categoria", formulario);
        model.addAttribute("categoriaId", id);
        return "admin-categoria-form";
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("categoria") CategoriaForm formulario,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (resultado.hasErrors()) {
            model.addAttribute("categoriaId", id);
            return "admin-categoria-form";
        }

        try {
            categoriaService.actualizar(id, formulario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Categoría actualizada correctamente.");
            return "redirect:/admin/categorias";
        } catch (IllegalArgumentException e) {
            resultado.rejectValue("nombre", "categoria.duplicada", e.getMessage());
            model.addAttribute("categoriaId", id);
            return "admin-categoria-form";
        }
    }

    private Categoria obtenerO404(Long id) {
        try {
            return categoriaService.obtener(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
