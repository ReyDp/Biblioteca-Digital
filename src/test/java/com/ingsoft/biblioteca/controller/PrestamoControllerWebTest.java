package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.config.SecurityConfig;
import com.ingsoft.biblioteca.entity.*;
import com.ingsoft.biblioteca.repository.ClienteRepository;
import com.ingsoft.biblioteca.repository.UsuarioRepository;
import com.ingsoft.biblioteca.security.CustomUserDetailsService;
import com.ingsoft.biblioteca.service.MultaService;
import com.ingsoft.biblioteca.service.PagoMultaService;
import com.ingsoft.biblioteca.service.PrestamoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrestamoController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class PrestamoControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ClienteRepository clienteRepository;
    @MockitoBean private UsuarioRepository usuarioRepository;
    @MockitoBean private PrestamoService prestamoService;
    @MockitoBean private MultaService multaService;
    @MockitoBean private PagoMultaService pagoMultaService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    void redirigeAlLoginCuandoNoHayAutenticacion() throws Exception {
        mockMvc.perform(get("/prestamos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void muestraSoloLosPrestamosDelClienteAutenticado() throws Exception {
        Cliente cliente = cliente(1L);
        Prestamo p = prestamo(cliente);
        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(Optional.of(cliente));
        when(prestamoService.listarPrestamosDeCliente(1L))
                .thenReturn(List.of(p));
        when(multaService.obtenerMultaPorPrestamoId(p.getId()))
                .thenReturn(Optional.empty());
        when(multaService.consultarMultaEstimada(p))
                .thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/prestamos")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("prestamos"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Libro de prueba")));

        verify(prestamoService).listarPrestamosDeCliente(1L);
    }

    @Test
    void verificaPropiedadAntesDeMostrarElDetalle() throws Exception {
        Cliente cliente = cliente(1L);
        Prestamo p = prestamo(cliente);
        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(Optional.of(cliente));
        when(prestamoService.obtenerPrestamoDeCliente(1L, 7L))
                .thenReturn(p);
        when(multaService.consultarMultaEstimada(p))
                .thenReturn(BigDecimal.ZERO);
        when(multaService.obtenerMultaPorPrestamoId(7L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/prestamos/7")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("detalle-prestamo"));

        verify(prestamoService).obtenerPrestamoDeCliente(1L, 7L);
    }

    @Test
    void impideQueUnClienteAccedaAlPanelAdministrativo() throws Exception {
        mockMvc.perform(get("/admin/prestamos")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void unAdministradorNoAccedeARutaDeCliente() throws Exception {
        mockMvc.perform(get("/prestamos")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void permiteAlAdministradorVerPrestamosActivos() throws Exception {
        when(prestamoService.listarPrestamosActivos()).thenReturn(List.of());

        mockMvc.perform(get("/admin/prestamos")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-prestamos"));
    }

    @Test
    void permiteAlAdministradorRegistrarUnaDevolucionConCsrf() throws Exception {
        Prestamo prestamo = prestamo(cliente(1L));
        when(prestamoService.registrarDevolucion(7L)).thenReturn(prestamo);

        mockMvc.perform(post("/admin/prestamos/7/devolucion")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/prestamos"));

        verify(prestamoService).registrarDevolucion(7L);
    }

    @Test
    void registrarDevolucionSinCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/admin/prestamos/7/devolucion")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(prestamoService);
    }

    @Test
    void solicitarPrestamoConCsrfFunciona() throws Exception {
        Cliente cliente = cliente(1L);
        Prestamo p = prestamo(cliente);
        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(Optional.of(cliente));
        when(prestamoService.registrarPrestamo(1L, 5L)).thenReturn(p);

        mockMvc.perform(post("/prestamos")
                        .with(user("cliente@example.com").roles("CLIENTE"))
                        .with(csrf())
                        .param("libroId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/libros/5"));

        verify(prestamoService).registrarPrestamo(1L, 5L);
    }

    @Test
    void solicitarPrestamoSinCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/prestamos")
                        .with(user("cliente@example.com").roles("CLIENTE"))
                        .param("libroId", "5"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(prestamoService);
    }

    @Test
    void detalleMuestraMultaEstimadaParaPrestamoActivo() throws Exception {
        Cliente cliente = cliente(1L);
        Prestamo p = prestamo(cliente);
        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(Optional.of(cliente));
        when(prestamoService.obtenerPrestamoDeCliente(1L, 7L))
                .thenReturn(p);
        when(multaService.consultarMultaEstimada(p))
                .thenReturn(new BigDecimal("3000.00"));
        when(multaService.obtenerMultaPorPrestamoId(7L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/prestamos/7")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("detalle-prestamo"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("MULTA ESTIMADA")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("3000.00")));

        verify(multaService).consultarMultaEstimada(p);
    }

    @Test
    void detalleMuestraMultaPendienteParaPrestamoDevuelto() throws Exception {
        Cliente cliente = cliente(1L);
        Prestamo p = prestamoDevuelto(cliente);
        Multa multa = nuevaMulta(p, new BigDecimal("5000.00"), 5, EstadoMulta.PENDIENTE);

        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(Optional.of(cliente));
        when(prestamoService.obtenerPrestamoDeCliente(1L, 7L))
                .thenReturn(p);
        when(multaService.consultarMultaEstimada(p))
                .thenReturn(new BigDecimal("5000.00"));
        when(multaService.obtenerMultaPorPrestamoId(7L))
                .thenReturn(Optional.of(multa));
        when(pagoMultaService.consultarSaldoPendiente(multa.getId()))
                .thenReturn(new BigDecimal("5000.00"));

        mockMvc.perform(get("/prestamos/7")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("detalle-prestamo"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("MULTA")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("5000.00")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("PENDIENTE")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Días de retraso")));

        verify(multaService).obtenerMultaPorPrestamoId(7L);
    }

    @Test
    void detalleMuestraMultaPagadaParaPrestamoDevuelto() throws Exception {
        Cliente cliente = cliente(1L);
        Prestamo p = prestamoDevuelto(cliente);
        Multa multa = nuevaMulta(p, BigDecimal.ZERO, 0, EstadoMulta.PAGADA);

        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(Optional.of(cliente));
        when(prestamoService.obtenerPrestamoDeCliente(1L, 7L))
                .thenReturn(p);
        when(multaService.consultarMultaEstimada(p))
                .thenReturn(BigDecimal.ZERO);
        when(multaService.obtenerMultaPorPrestamoId(7L))
                .thenReturn(Optional.of(multa));

        mockMvc.perform(get("/prestamos/7")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("detalle-prestamo"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("PAGADA")));

        verify(multaService).obtenerMultaPorPrestamoId(7L);
    }

    @Test
    void detalleMuestraMultaEstimadaCeroParaPrestamoActivoSinRetraso() throws Exception {
        Cliente cliente = cliente(1L);
        Prestamo p = prestamo(cliente);
        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(Optional.of(cliente));
        when(prestamoService.obtenerPrestamoDeCliente(1L, 7L))
                .thenReturn(p);
        when(multaService.consultarMultaEstimada(p))
                .thenReturn(BigDecimal.ZERO);
        when(multaService.obtenerMultaPorPrestamoId(7L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/prestamos/7")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("detalle-prestamo"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("MULTA ESTIMADA")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("0.00")));
    }

    @Test
    void listadoMuestraIndicadorDeMultaEstimadaParaPrestamoActivo() throws Exception {
        Cliente cliente = cliente(1L);
        Prestamo p = prestamo(cliente);
        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(Optional.of(cliente));
        when(prestamoService.listarPrestamosDeCliente(1L))
                .thenReturn(List.of(p));
        when(multaService.obtenerMultaPorPrestamoId(p.getId()))
                .thenReturn(Optional.empty());
        when(multaService.consultarMultaEstimada(p))
                .thenReturn(new BigDecimal("2000.00"));

        mockMvc.perform(get("/prestamos")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("prestamos"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Multa estimada")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2000.00")));
    }

    @Test
    void listadoMuestraMultaDefinitivaParaPrestamoDevuelto() throws Exception {
        Cliente cliente = cliente(1L);
        Prestamo p = prestamoDevuelto(cliente);
        Multa multa = nuevaMulta(p, new BigDecimal("3000.00"), 3, EstadoMulta.PENDIENTE);

        when(clienteRepository.findByUsuarioCorreo("cliente@example.com"))
                .thenReturn(Optional.of(cliente));
        when(prestamoService.listarPrestamosDeCliente(1L))
                .thenReturn(List.of(p));
        when(multaService.obtenerMultaPorPrestamoId(p.getId()))
                .thenReturn(Optional.of(multa));

        mockMvc.perform(get("/prestamos")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("prestamos"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Multa")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("3000.00")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("PENDIENTE")));
    }

    private Cliente cliente(Long id) {
        Cliente cliente = new Cliente();
        org.springframework.test.util.ReflectionTestUtils.setField(cliente, "id", id);
        cliente.setDocumento("12345");
        Usuario usuario = new Usuario();
        usuario.setNombre("Cliente de prueba");
        cliente.setUsuario(usuario);
        return cliente;
    }

    private Prestamo prestamo(Cliente cliente) {
        Libro libro = new Libro();
        libro.setTitulo("Libro de prueba");
        libro.setAutor("Autora de prueba");

        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaLimite(LocalDate.now().plusDays(15));
        return prestamo;
    }

    private Prestamo prestamoDevuelto(Cliente cliente) {
        Libro libro = new Libro();
        libro.setTitulo("Libro de prueba");
        libro.setAutor("Autora de prueba");

        Prestamo prestamo = new Prestamo();
        org.springframework.test.util.ReflectionTestUtils.setField(prestamo, "id", 7L);
        prestamo.setCliente(cliente);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(LocalDate.now().minusDays(20));
        prestamo.setFechaLimite(LocalDate.now().minusDays(5));
        prestamo.setFechaDevolucion(LocalDate.now());
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        return prestamo;
    }

    private Multa nuevaMulta(
            Prestamo prestamo,
            BigDecimal importeTotal,
            int diasRetraso,
            EstadoMulta estado
    ) {
        Multa multa = new Multa();
        org.springframework.test.util.ReflectionTestUtils.setField(multa, "id", 1L);
        multa.setPrestamo(prestamo);
        multa.setImporteTotal(importeTotal);
        multa.setDiasRetrasoFinales(diasRetraso);
        multa.setEstado(estado);
        multa.setFechaCalculo(LocalDate.now());
        return multa;
    }
}
