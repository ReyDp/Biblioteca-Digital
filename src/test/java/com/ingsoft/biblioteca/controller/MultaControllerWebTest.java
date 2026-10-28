package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.config.SecurityConfig;
import com.ingsoft.biblioteca.entity.Cliente;
import com.ingsoft.biblioteca.entity.Multa;
import com.ingsoft.biblioteca.entity.Prestamo;
import com.ingsoft.biblioteca.entity.Usuario;
import com.ingsoft.biblioteca.repository.ClienteRepository;
import com.ingsoft.biblioteca.repository.UsuarioRepository;
import com.ingsoft.biblioteca.service.MultaService;
import com.ingsoft.biblioteca.service.PagoMultaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MultaController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class MultaControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ClienteRepository clienteRepository;
    @MockitoBean private UsuarioRepository usuarioRepository;
    @MockitoBean private MultaService multaService;
    @MockitoBean private PagoMultaService pagoMultaService;
    @MockitoBean private com.ingsoft.biblioteca.security.CustomUserDetailsService customUserDetailsService;

    @Test
    void unaPersonaNoAutenticadaNoPuedeConsultarMultas() throws Exception {
        mockMvc.perform(get("/multas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void clienteConsultaSuListado() throws Exception {
        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(java.util.Optional.of(new com.ingsoft.biblioteca.entity.Cliente()));
        when(multaService.listarMultasDeCliente(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/multas")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("multas"));
    }

    @Test
    void clienteNoPuedeConsultarPanelAdministrativo() throws Exception {
        mockMvc.perform(get("/admin/multas")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorPuedeConsultarMultas() throws Exception {
        when(multaService.listarTodas()).thenReturn(List.of());

        mockMvc.perform(get("/admin/multas")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-multas"));
    }

    @Test
    void registrarPagoConCsrfFunciona() throws Exception {
        Cliente cliente = new Cliente();
        org.springframework.test.util.ReflectionTestUtils.setField(cliente, "id", 1L);
        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(java.util.Optional.of(cliente));

        Multa multa = nuevaMulta(cliente);
        when(multaService.obtenerMultaDeCliente(1L, 10L)).thenReturn(multa);
        when(pagoMultaService.consultarSaldoPendiente(10L))
                .thenReturn(new BigDecimal("5000"));

        mockMvc.perform(post("/multas/10/pagos")
                        .with(user("cliente@example.com").roles("CLIENTE"))
                        .with(csrf())
                        .param("valor", "2000")
                        .param("metodo", "Transferencia")
                        .param("referencia", "REF-001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/multas/10"));

        verify(pagoMultaService).registrarPago(eq(10L), eq(new BigDecimal("2000")),
                eq("Transferencia"), eq("REF-001"));
    }

    @Test
    void registrarPagoSinCsrfEsRechazado() throws Exception {
        Cliente cliente = new Cliente();
        org.springframework.test.util.ReflectionTestUtils.setField(cliente, "id", 1L);
        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(java.util.Optional.of(cliente));

        mockMvc.perform(post("/multas/10/pagos")
                        .with(user("cliente@example.com").roles("CLIENTE"))
                        .param("valor", "2000")
                        .param("metodo", "Transferencia"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pagoMultaService);
    }

    @Test
    void registrarPagoConValorInvalidoMuestraError() throws Exception {
        Cliente cliente = new Cliente();
        org.springframework.test.util.ReflectionTestUtils.setField(cliente, "id", 1L);
        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(java.util.Optional.of(cliente));

        Multa multa = nuevaMulta(cliente);
        when(multaService.obtenerMultaDeCliente(1L, 10L)).thenReturn(multa);

        mockMvc.perform(post("/multas/10/pagos")
                        .with(user("cliente@example.com").roles("CLIENTE"))
                        .with(csrf())
                        .param("valor", "")
                        .param("metodo", "Efectivo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/multas/10"))
                .andExpect(flash().attributeExists("mensajeError"));

        verifyNoInteractions(pagoMultaService);
    }

    private Cliente cliente(Long id) {
        Cliente c = new Cliente();
        org.springframework.test.util.ReflectionTestUtils.setField(c, "id", id);
        c.setDocumento("12345");
        Usuario u = new Usuario();
        u.setNombre("Cliente prueba");
        c.setUsuario(u);
        return c;
    }

    private Multa nuevaMulta(Cliente cliente) {
        Prestamo prestamo = new Prestamo();
        com.ingsoft.biblioteca.entity.Libro libro = new com.ingsoft.biblioteca.entity.Libro();
        libro.setTitulo("Libro prueba");
        prestamo.setLibro(libro);
        prestamo.setCliente(cliente);

        Multa multa = new Multa();
        multa.setPrestamo(prestamo);
        multa.setDiasRetrasoFinales(5);
        multa.setImporteTotal(new BigDecimal("5000"));
        return multa;
    }
}
