package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.dto.PagoMultaForm;
import com.ingsoft.biblioteca.entity.Multa;
import com.ingsoft.biblioteca.repository.ClienteRepository;
import com.ingsoft.biblioteca.service.MultaService;
import com.ingsoft.biblioteca.service.PagoMultaService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MultaController {

    private final ClienteRepository clienteRepository;
    private final MultaService multaService;
    private final PagoMultaService pagoMultaService;

    public MultaController(
            ClienteRepository clienteRepository,
            MultaService multaService,
            PagoMultaService pagoMultaService
    ) {
        this.clienteRepository = clienteRepository;
        this.multaService = multaService;
        this.pagoMultaService = pagoMultaService;
    }

    @GetMapping("/multas")
    public String listar(Authentication authentication, Model model) {
        Long clienteId = obtenerClienteId(authentication);
        model.addAttribute("multas", multaService.listarMultasDeCliente(clienteId));
        return "multas";
    }

    @GetMapping("/multas/{multaId}")
    public String detalle(
            @PathVariable Long multaId,
            Authentication authentication,
            Model model
    ) {
        Long clienteId = obtenerClienteId(authentication);
        Multa multa = multaService.obtenerMultaDeCliente(clienteId, multaId);
        model.addAttribute("multa", multa);
        model.addAttribute("saldo", pagoMultaService.consultarSaldoPendiente(multaId));
        model.addAttribute("pagos", pagoMultaService.listarPagos(multaId));
        if (!model.containsAttribute("pago")) {
            model.addAttribute("pago", new PagoMultaForm());
        }
        return "detalle-multa";
    }

    @PostMapping("/multas/{multaId}/pagos")
    public String registrarPago(
            @PathVariable Long multaId,
            Authentication authentication,
            @Valid @ModelAttribute("pago") PagoMultaForm formulario,
            BindingResult resultado,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Long clienteId = obtenerClienteId(authentication);
            multaService.obtenerMultaDeCliente(clienteId, multaId);
            if (resultado.hasErrors()) {
                redirectAttributes.addFlashAttribute("mensajeError", "Revisa los datos del pago.");
                return "redirect:/multas/" + multaId;
            }

            pagoMultaService.registrarPago(
                    multaId,
                    formulario.getValor(),
                    formulario.getMetodo(),
                    formulario.getReferencia()
            );
            redirectAttributes.addFlashAttribute("mensajeExito", "Pago registrado correctamente.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/multas/" + multaId;
    }

    @GetMapping("/admin/multas")
    public String listarAdministracion(Model model) {
        model.addAttribute("multas", multaService.listarTodas());
        return "admin-multas";
    }

    private Long obtenerClienteId(Authentication authentication) {
        return clienteRepository.findByUsuarioCorreo(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Tu cuenta todavía no tiene un perfil de cliente."
                ))
                .getId();
    }
}
