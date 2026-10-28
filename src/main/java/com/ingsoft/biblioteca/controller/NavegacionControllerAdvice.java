package com.ingsoft.biblioteca.controller;

import com.ingsoft.biblioteca.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class NavegacionControllerAdvice {

    private final UsuarioRepository usuarioRepository;

    public NavegacionControllerAdvice(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute("nombreUsuarioAutenticado")
    public String nombreUsuarioAutenticado(Authentication authentication) {
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            return "";
        }

        return usuarioRepository.findByCorreo(authentication.getName())
                .map(usuario -> usuario.getNombre())
                .orElse("");
    }
}
