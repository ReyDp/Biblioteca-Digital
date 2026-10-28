package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.dto.RegistroUsuarioForm;
import com.ingsoft.biblioteca.entity.Cliente;
import com.ingsoft.biblioteca.entity.RolUsuario;
import com.ingsoft.biblioteca.entity.Usuario;
import com.ingsoft.biblioteca.repository.ClienteRepository;
import com.ingsoft.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, clienteRepository, passwordEncoder);
    }

    @Test
    void registraClienteConContrasenaBCryptYRolCliente() {
        RegistroUsuarioForm formulario = formularioValido();
        when(usuarioRepository.existsByCorreo("ana@example.com")).thenReturn(false);
        when(clienteRepository.existsByDocumento("12345")).thenReturn(false);
        when(passwordEncoder.encode("clave-segura")).thenReturn("$2a$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.registrarCliente(formulario);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario usuario = usuarioCaptor.getValue();
        assertEquals("ana@example.com", usuario.getCorreo());
        assertEquals("$2a$hash", usuario.getPasswordHash());
        assertEquals(RolUsuario.CLIENTE, usuario.getRol());
        assertTrue(usuario.isActivo());
        verify(passwordEncoder).encode("clave-segura");
    }

    @Test
    void rechazaCorreosDuplicados() {
        when(usuarioRepository.existsByCorreo("ana@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarCliente(formularioValido()));

        verifyNoInteractions(passwordEncoder);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void rechazaDocumentoDuplicado() {
        when(usuarioRepository.existsByCorreo("ana@example.com")).thenReturn(false);
        when(clienteRepository.existsByDocumento("12345")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarCliente(formularioValido()));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void rechazaContrasenasDiferentes() {
        RegistroUsuarioForm formulario = formularioValido();
        formulario.setConfirmacionPassword("otra-clave");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarCliente(formulario));

        verifyNoInteractions(usuarioRepository, clienteRepository, passwordEncoder);
    }

    @Test
    void obtienePerfilPorCorreoNormalizado() {
        Cliente cliente = new Cliente();
        when(clienteRepository.findByUsuarioCorreo("ana@example.com")).thenReturn(Optional.of(cliente));

        assertSame(cliente, usuarioService.obtenerPerfil(" ANA@EXAMPLE.COM "));
    }

    private RegistroUsuarioForm formularioValido() {
        RegistroUsuarioForm formulario = new RegistroUsuarioForm();
        formulario.setNombre(" Ana Pérez ");
        formulario.setCorreo(" ANA@EXAMPLE.COM ");
        formulario.setDocumento(" 12345 ");
        formulario.setTelefono(" 300 000 0000 ");
        formulario.setPassword("clave-segura");
        formulario.setConfirmacionPassword("clave-segura");
        return formulario;
    }
}
