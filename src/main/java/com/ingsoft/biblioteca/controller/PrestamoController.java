package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.entity.Prestamo;
import com.ingsoft.biblioteca.repository.ClienteRepository;
import com.ingsoft.biblioteca.service.PrestamoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PrestamoController {

    private final ClienteRepository clienteRepository;
    private final PrestamoService prestamoService;

    public PrestamoController(
            ClienteRepository clienteRepository,
            PrestamoService prestamoService
    ) {
        this.clienteRepository = clienteRepository;
        this.prestamoService = prestamoService;
    }

    @PostMapping("/prestamos")
    public String solicitarPrestamo(
            @RequestParam Long libroId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        String correo = authentication.getName();

        try {
            var cliente = clienteRepository.findByUsuarioCorreo(correo)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Tu cuenta todavía no tiene un perfil de cliente."
                            )
                    );

            Prestamo prestamo = prestamoService.registrarPrestamo(
                    cliente.getId(),
                    libroId
            );

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Préstamo registrado correctamente. Fecha límite: "
                            + prestamo.getFechaLimite()
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );
        }

        return "redirect:/libros/" + libroId;
    }
}