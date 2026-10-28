package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.dto.PerfilClienteForm;
import com.ingsoft.biblioteca.dto.RegistroUsuarioForm;
import com.ingsoft.biblioteca.entity.Cliente;
import com.ingsoft.biblioteca.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        if (!model.containsAttribute("registro")) {
            model.addAttribute("registro", new RegistroUsuarioForm());
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(
            @Valid @ModelAttribute("registro") RegistroUsuarioForm formulario,
            BindingResult resultado,
            RedirectAttributes redirectAttributes
    ) {
        if (resultado.hasErrors()) {
            return "registro";
        }

        try {
            usuarioService.registrarCliente(formulario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cuenta creada. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            resultado.reject("registro", e.getMessage());
            return "registro";
        }
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(Authentication authentication, Model model) {
        Cliente cliente = usuarioService.obtenerPerfil(authentication.getName());
        model.addAttribute("cliente", cliente);
        model.addAttribute("perfil", crearFormularioPerfil(cliente));
        return "perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(
            Authentication authentication,
            @Valid @ModelAttribute("perfil") PerfilClienteForm formulario,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Cliente cliente = usuarioService.obtenerPerfil(authentication.getName());
        model.addAttribute("cliente", cliente);

        if (resultado.hasErrors()) {
            return "perfil";
        }

        try {
            usuarioService.actualizarPerfil(authentication.getName(), formulario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Perfil actualizado correctamente.");
            return "redirect:/perfil";
        } catch (IllegalArgumentException e) {
            resultado.reject("perfil", e.getMessage());
            return "perfil";
        }
    }

    private PerfilClienteForm crearFormularioPerfil(Cliente cliente) {
        PerfilClienteForm formulario = new PerfilClienteForm();
        formulario.setNombre(cliente.getUsuario().getNombre());
        formulario.setDocumento(cliente.getDocumento());
        formulario.setTelefono(cliente.getTelefono());
        return formulario;
    }
}
