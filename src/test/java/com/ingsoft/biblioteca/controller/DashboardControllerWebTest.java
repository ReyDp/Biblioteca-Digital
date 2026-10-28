package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.config.SecurityConfig;
import com.ingsoft.biblioteca.dto.DashboardStats;
import com.ingsoft.biblioteca.repository.ClienteRepository;
import com.ingsoft.biblioteca.repository.UsuarioRepository;
import com.ingsoft.biblioteca.security.CustomUserDetailsService;
import com.ingsoft.biblioteca.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class DashboardControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void redirigeAlLoginCuandoNoHayAutenticacion() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void impideQueUnClienteAccedaAlDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void permiteAlAdministradorVerElDashboard() throws Exception {
        DashboardStats stats = new DashboardStats();
        stats.setTotalEjemplares(50L);
        stats.setEjemplaresDisponibles(33L);
        stats.setEjemplaresInactivos(5L);
        stats.setPrestamosActivos(12L);
        stats.setPrestamosVencidos(3L);
        stats.setPrestamosDevueltos(20L);
        stats.setMultasPendientes(7L);
        stats.setSaldoTotalPendiente(new BigDecimal("10000.00"));

        when(dashboardService.obtenerEstadisticas()).thenReturn(stats);

        mockMvc.perform(get("/admin/dashboard")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-dashboard"))
                .andExpect(model().attribute("stats", stats));

        verify(dashboardService).obtenerEstadisticas();
    }

    @Test
    void dashboardMuestraLosOchoIndicadores() throws Exception {
        DashboardStats stats = new DashboardStats();
        stats.setTotalEjemplares(50L);
        stats.setEjemplaresDisponibles(33L);
        stats.setEjemplaresInactivos(5L);
        stats.setPrestamosActivos(12L);
        stats.setPrestamosVencidos(3L);
        stats.setPrestamosDevueltos(20L);
        stats.setMultasPendientes(7L);
        stats.setSaldoTotalPendiente(new BigDecimal("10000.00"));

        when(dashboardService.obtenerEstadisticas()).thenReturn(stats);

        mockMvc.perform(get("/admin/dashboard")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-dashboard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Panel general")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Total de ejemplares")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ejemplares disponibles")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ejemplares inactivos")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Préstamos activos")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Préstamos vencidos")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Préstamos devueltos")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Multas pendientes")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Saldo total pendiente")));
    }

    @Test
    void dashboardConDatosVaciosMuestraCeros() throws Exception {
        DashboardStats stats = new DashboardStats();
        stats.setTotalEjemplares(0L);
        stats.setEjemplaresDisponibles(0L);
        stats.setEjemplaresInactivos(0L);
        stats.setPrestamosActivos(0L);
        stats.setPrestamosVencidos(0L);
        stats.setPrestamosDevueltos(0L);
        stats.setMultasPendientes(0L);
        stats.setSaldoTotalPendiente(BigDecimal.ZERO);

        when(dashboardService.obtenerEstadisticas()).thenReturn(stats);

        mockMvc.perform(get("/admin/dashboard")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-dashboard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("0")));
    }
}
