package com.ingsoft.biblioteca.security;

import com.ingsoft.biblioteca.entity.RolUsuario;
import com.ingsoft.biblioteca.entity.Usuario;
import com.ingsoft.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        customUserDetailsService = new CustomUserDetailsService(usuarioRepository);
    }

    @Test
    void cargaUsuarioActivoConRolCliente() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("ana@example.com");
        usuario.setNombre("Ana García");
        usuario.setPasswordHash("$2a$hash");
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setActivo(true);

        when(usuarioRepository.findByCorreo("ana@example.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("ana@example.com");

        assertEquals("ana@example.com", userDetails.getUsername());
        assertEquals("$2a$hash", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE")));
        verify(usuarioRepository).findByCorreo("ana@example.com");
    }

    @Test
    void usuarioInactivoEsDeshabilitado() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("desactivado@example.com");
        usuario.setNombre("Usuario desactivado");
        usuario.setPasswordHash("$2a$hash");
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setActivo(false);

        when(usuarioRepository.findByCorreo("desactivado@example.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("desactivado@example.com");

        assertFalse(userDetails.isEnabled());
        verify(usuarioRepository).findByCorreo("desactivado@example.com");
    }

    @Test
    void usuarioInexistenteLanzaExcepcion() {
        when(usuarioRepository.findByCorreo("inexistente@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("inexistente@example.com")
        );

        verify(usuarioRepository).findByCorreo("inexistente@example.com");
    }

    @Test
    void cargaUsuarioAdministradorConRolAdmin() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("admin@example.com");
        usuario.setNombre("Administrador");
        usuario.setPasswordHash("$2a$hash");
        usuario.setRol(RolUsuario.ADMIN);
        usuario.setActivo(true);

        when(usuarioRepository.findByCorreo("admin@example.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@example.com");

        assertEquals("admin@example.com", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
