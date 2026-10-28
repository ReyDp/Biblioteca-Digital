package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.entity.Multa;
import com.ingsoft.biblioteca.entity.Prestamo;
import com.ingsoft.biblioteca.repository.ClienteRepository;
import com.ingsoft.biblioteca.service.MultaService;
import com.ingsoft.biblioteca.service.PagoMultaService;
import com.ingsoft.biblioteca.service.PrestamoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PrestamoController {

    private final ClienteRepository clienteRepository;
    private final PrestamoService prestamoService;
    private final MultaService multaService;
    private final PagoMultaService pagoMultaService;

    public PrestamoController(
            ClienteRepository clienteRepository,
            PrestamoService prestamoService,
            MultaService multaService,
            PagoMultaService pagoMultaService
    ) {
        this.clienteRepository = clienteRepository;
        this.prestamoService = prestamoService;
        this.multaService = multaService;
        this.pagoMultaService = pagoMultaService;
    }

    @GetMapping("/prestamos")
    public String listarPrestamos(
            Authentication authentication,
            Model model
    ) {
        var cliente = obtenerCliente(authentication);
        model.addAttribute(
                "prestamos",
                prestamoService.listarPrestamosDeCliente(cliente.getId())
        );
        model.addAttribute("multaService", multaService);
        model.addAttribute("pagoMultaService", pagoMultaService);
        return "prestamos";
    }

    @GetMapping("/prestamos/{prestamoId}")
    public String detallePrestamo(
            @PathVariable Long prestamoId,
            Authentication authentication,
            Model model
    ) {
        var cliente = obtenerCliente(authentication);
        Prestamo prestamo = prestamoService.obtenerPrestamoDeCliente(
                cliente.getId(),
                prestamoId
        );
        model.addAttribute("prestamo", prestamo);
        model.addAttribute(
                "multaEstimada",
                multaService.consultarMultaEstimada(prestamo)
        );
        model.addAttribute(
                "multa",
                multaService.obtenerMultaPorPrestamoId(prestamoId)
        );
        model.addAttribute("pagoMultaService", pagoMultaService);
        return "detalle-prestamo";
    }

    @PostMapping("/prestamos")
    public String solicitarPrestamo(
            @RequestParam Long libroId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            var cliente = obtenerCliente(authentication);

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

    @GetMapping("/admin/prestamos")
    public String listarPrestamosParaDevolucion(Model model) {
        model.addAttribute(
                "prestamos",
                prestamoService.listarPrestamosActivos()
        );
        return "admin-prestamos";
    }

    @PostMapping("/admin/prestamos/{prestamoId}/devolucion")
    public String registrarDevolucion(
            @PathVariable Long prestamoId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Prestamo prestamo = prestamoService.registrarDevolucion(prestamoId);
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Devolución registrada correctamente."
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/prestamos";
    }

    private com.ingsoft.biblioteca.entity.Cliente obtenerCliente(
            Authentication authentication
    ) {
        return clienteRepository.findByUsuarioCorreo(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Tu cuenta todavía no tiene un perfil de cliente."
                ));
    }
}
