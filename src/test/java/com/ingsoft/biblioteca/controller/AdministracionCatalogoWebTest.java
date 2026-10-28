package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.config.SecurityConfig;
import com.ingsoft.biblioteca.dto.CategoriaForm;
import com.ingsoft.biblioteca.dto.LibroForm;
import com.ingsoft.biblioteca.repository.UsuarioRepository;
import com.ingsoft.biblioteca.security.CustomUserDetailsService;
import com.ingsoft.biblioteca.service.CategoriaService;
import com.ingsoft.biblioteca.service.LibroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AdminLibroController.class, AdminCategoriaController.class})
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class AdministracionCatalogoWebTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private LibroService libroService;
    @MockitoBean private CategoriaService categoriaService;
    @MockitoBean private UsuarioRepository usuarioRepository;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    void visitanteEsRedirigidoYClienteNoAccedeALaAdministracion() throws Exception {
        mockMvc.perform(get("/admin/libros"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/admin/categorias")
                        .with(user("cliente@example.com").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorPuedeVerLibrosYCategorias() throws Exception {
        when(libroService.buscarParaAdministracion("")).thenReturn(List.of());
        when(libroService.calcularDisponibilidades(List.of())).thenReturn(Map.of());
        when(categoriaService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/admin/libros")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-libros"))
                .andExpect(content().string(containsString("Registrar libro")));

        mockMvc.perform(get("/admin/categorias")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-categorias"))
                .andExpect(content().string(containsString("Crear categoría")));
    }

    @Test
    void formularioDeLibroInvalidoNoCreaRegistro() throws Exception {
        when(categoriaService.listar()).thenReturn(List.of());

        mockMvc.perform(post("/admin/libros")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .param("titulo", "")
                        .param("autor", "Autora")
                        .param("categoriaId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-libro-form"))
                .andExpect(content().string(containsString("El título es obligatorio.")));

        verify(libroService, never()).crear(any(LibroForm.class));
    }

    @Test
    void duplicadoDeCategoriaSeMuestraEnElFormulario() throws Exception {
        doThrow(new IllegalArgumentException("Ya existe una categoría con ese nombre."))
                .when(categoriaService).crear(any(CategoriaForm.class));

        mockMvc.perform(post("/admin/categorias")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .param("nombre", "Historia")
                        .param("descripcion", "Lecturas históricas"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-categoria-form"))
                .andExpect(content().string(containsString("Ya existe una categoría con ese nombre.")));
    }

    @Test
    void postAdministrativoSinCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/admin/libros")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .param("titulo", "Libro")
                        .param("autor", "Autor")
                        .param("categoriaId", "1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(libroService);
    }

    @Test
    void postCategoriaSinCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/admin/categorias")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .param("nombre", "Historia")
                        .param("descripcion", "Lecturas históricas"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(categoriaService);
    }
}
