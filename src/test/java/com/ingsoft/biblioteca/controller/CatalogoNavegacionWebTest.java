package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.config.SecurityConfig;
import com.ingsoft.biblioteca.entity.Usuario;
import com.ingsoft.biblioteca.repository.CategoriaRepository;
import com.ingsoft.biblioteca.repository.LibroRepository;
import com.ingsoft.biblioteca.repository.PrestamoRepository;
import com.ingsoft.biblioteca.repository.UsuarioRepository;
import com.ingsoft.biblioteca.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogoController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class CatalogoNavegacionWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private LibroRepository libroRepository;
    @MockitoBean private CategoriaRepository categoriaRepository;
    @MockitoBean private PrestamoRepository prestamoRepository;
    @MockitoBean private UsuarioRepository usuarioRepository;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    void visitanteVeEnlacesDeAccesoYRegistro() throws Exception {
        prepararCatalogoVacio();

        mockMvc.perform(get("/catalogo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Iniciar sesión")))
                .andExpect(content().string(containsString("Registrarse")))
                .andExpect(content().string(not(containsString("Mis préstamos"))));
    }

    @Test
    void clienteVeSuNombreYOpcionesDeCuenta() throws Exception {
        prepararCatalogoVacio();
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana García López");
        when(usuarioRepository.findByCorreo("ana@example.com"))
                .thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/catalogo")
                        .with(user("ana@example.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana García López")))
                .andExpect(content().string(containsString("Mi perfil")))
                .andExpect(content().string(containsString("Mis préstamos")))
                .andExpect(content().string(containsString("Mis multas")))
                .andExpect(content().string(containsString("/logout")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(not(containsString("Registrarse"))));
    }

    @Test
    void administradorVeSoloOpcionesAdministrativasDisponibles() throws Exception {
        prepararCatalogoVacio();
        Usuario usuario = new Usuario();
        usuario.setNombre("Cuenta de administración");
        when(usuarioRepository.findByCorreo("admin@example.com"))
                .thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/catalogo")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Administrador del sistema")))
                .andExpect(content().string(containsString("/admin/prestamos")))
                .andExpect(content().string(containsString("/admin/multas")))
                .andExpect(content().string(not(containsString("Mi perfil"))))
                .andExpect(content().string(not(containsString("Iniciar sesión"))));
    }

    private void prepararCatalogoVacio() {
        when(libroRepository.buscarCatalogo("", 0L)).thenReturn(List.of());
        when(categoriaRepository.findAll()).thenReturn(List.of());
    }
}
