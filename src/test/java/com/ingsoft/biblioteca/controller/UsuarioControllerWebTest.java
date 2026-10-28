package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.config.SecurityConfig;
import com.ingsoft.biblioteca.entity.Cliente;
import com.ingsoft.biblioteca.entity.Usuario;
import com.ingsoft.biblioteca.repository.ClienteRepository;
import com.ingsoft.biblioteca.repository.UsuarioRepository;
import com.ingsoft.biblioteca.service.UsuarioService;
import com.ingsoft.biblioteca.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class UsuarioControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void muestraFormularioDeRegistroSinErroresDePlantilla() throws Exception {
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"))
                .andExpect(content().string(containsString("Crea tu cuenta")));
    }

    @Test
    void registrarConCsrfFunciona() throws Exception {
        mockMvc.perform(post("/registro")
                        .with(csrf())
                        .param("nombre", "Ana García")
                        .param("correo", "ana@example.com")
                        .param("password", "contrasena123")
                        .param("confirmacionPassword", "contrasena123")
                        .param("documento", "12345678")
                        .param("telefono", "3001234567"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("mensajeExito", "Cuenta creada. Ya puedes iniciar sesión."));

        verify(usuarioService).registrarCliente(any());
    }

    @Test
    void registrarSinCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/registro")
                        .param("nombre", "Ana García")
                        .param("correo", "ana@example.com")
                        .param("password", "contrasena123")
                        .param("confirmacionPassword", "contrasena123")
                        .param("documento", "12345678"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void actualizarPerfilConCsrfFunciona() throws Exception {
        Cliente cliente = new Cliente();
        org.springframework.test.util.ReflectionTestUtils.setField(cliente, "id", 1L);
        cliente.setDocumento("12345");
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana García");
        cliente.setUsuario(usuario);

        when(clienteRepository.findByUsuarioCorreo("ana@example.com"))
                .thenReturn(java.util.Optional.of(cliente));
        when(usuarioService.obtenerPerfil("ana@example.com")).thenReturn(cliente);

        mockMvc.perform(post("/perfil")
                        .with(user("ana@example.com").roles("CLIENTE"))
                        .with(csrf())
                        .param("nombre", "Ana García López")
                        .param("documento", "12345")
                        .param("telefono", "3009876543"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"))
                .andExpect(flash().attribute("mensajeExito", "Perfil actualizado correctamente."));

        verify(usuarioService).actualizarPerfil(eq("ana@example.com"), any());
    }

    @Test
    void actualizarPerfilSinCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/perfil")
                        .with(user("ana@example.com").roles("CLIENTE"))
                        .param("nombre", "Ana García López")
                        .param("documento", "12345"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }
}
