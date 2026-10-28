package com.ingsoft.biblioteca.service;

import com.ingsoft.biblioteca.dto.PerfilClienteForm;
import com.ingsoft.biblioteca.dto.RegistroUsuarioForm;
import com.ingsoft.biblioteca.entity.Cliente;
import com.ingsoft.biblioteca.entity.RolUsuario;
import com.ingsoft.biblioteca.entity.Usuario;
import com.ingsoft.biblioteca.repository.ClienteRepository;
import com.ingsoft.biblioteca.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Cliente registrarCliente(RegistroUsuarioForm formulario) {
        validarConfirmacionPassword(formulario);

        String correo = normalizarCorreo(formulario.getCorreo());
        String documento = limpiar(formulario.getDocumento());

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
        }
        if (clienteRepository.existsByDocumento(documento)) {
            throw new IllegalArgumentException("Ya existe un cliente con ese documento.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(limpiar(formulario.getNombre()));
        usuario.setCorreo(correo);
        usuario.setPasswordHash(passwordEncoder.encode(formulario.getPassword()));
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setActivo(true);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuarioGuardado);
        cliente.setDocumento(documento);
        cliente.setTelefono(limpiarOpcional(formulario.getTelefono()));
        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPerfil(String correo) {
        return clienteRepository.findByUsuarioCorreo(normalizarCorreo(correo))
                .orElseThrow(() -> new IllegalArgumentException("No existe un perfil de cliente para esta cuenta."));
    }

    @Transactional
    public Cliente actualizarPerfil(String correo, PerfilClienteForm formulario) {
        Cliente cliente = obtenerPerfil(correo);
        String documento = limpiar(formulario.getDocumento());

        clienteRepository.findByDocumento(documento)
                .filter(otroCliente -> !otroCliente.getId().equals(cliente.getId()))
                .ifPresent(otroCliente -> {
                    throw new IllegalArgumentException("Ya existe un cliente con ese documento.");
                });

        cliente.getUsuario().setNombre(limpiar(formulario.getNombre()));
        cliente.setDocumento(documento);
        cliente.setTelefono(limpiarOpcional(formulario.getTelefono()));
        return clienteRepository.save(cliente);
    }

    private void validarConfirmacionPassword(RegistroUsuarioForm formulario) {
        if (!formulario.getPassword().equals(formulario.getConfirmacionPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }
    }

    private String normalizarCorreo(String correo) {
        return limpiar(correo).toLowerCase(Locale.ROOT);
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String limpiarOpcional(String valor) {
        String resultado = limpiar(valor);
        return resultado.isEmpty() ? null : resultado;
    }
}
