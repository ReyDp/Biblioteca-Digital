package com.ingsoft.prestamos;

import com.ingsoft.prestamos.application.port.output.ClienteQueryPort;
import com.ingsoft.prestamos.application.port.output.ReservaLibroPort;
import com.ingsoft.prestamos.infrastructure.config.BibliotecaDigitalFallbackConfiguration;
import com.ingsoft.prestamos.infrastructure.config.BibliotecaDigitalHttpConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        BibliotecaDigitalHttpConfiguration.class,
        BibliotecaDigitalFallbackConfiguration.class
})
class BeanWireValidationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void clienteQueryPort_beanExists() {
        assertThat(applicationContext.getBean(ClienteQueryPort.class))
                .as("ClienteQueryPort debe estar disponible para que RegistrarPrestamoUseCase se cree")
                .isNotNull();
    }

    @Test
    void reservaLibroPort_beanExists() {
        assertThat(applicationContext.getBean(ReservaLibroPort.class))
                .as("ReservaLibroPort debe estar disponible para que RegistrarPrestamoUseCase se cree")
                .isNotNull();
    }

    @Test
    void fallbackConfiguration_isActive_whenUrlIsEmpty() {
        assertThat(applicationContext.containsBean("clienteQueryPort"))
                .as("Debe existir un bean ClienteQueryPort incluso sin biblioteca.digital.url")
                .isTrue();

        assertThat(applicationContext.containsBean("reservaLibroPort"))
                .as("Debe existir un bean ReservaLibroPort incluso sin biblioteca.digital.url")
                .isTrue();
    }

    @Test
    void fallbackBeans_areCreated_whenHttpConfigIsInactive() {
        boolean clienteQueryPort =
                applicationContext.containsBean("clienteQueryPort");

        boolean reservaLibroPort =
                applicationContext.containsBean("reservaLibroPort");

        boolean bibliotecaDigitalHttpClient =
                applicationContext.containsBean("bibliotecaDigitalHttpClient");

        assertThat(clienteQueryPort)
                .as("ClienteQueryPort debe existir (creado por fallback)")
                .isTrue();

        assertThat(reservaLibroPort)
                .as("ReservaLibroPort debe existir (creado por fallback)")
                .isTrue();

        assertThat(bibliotecaDigitalHttpClient)
                .as("BibliotecaDigitalHttpClient NO debe existir cuando la URL no está configurada")
                .isFalse();
    }
}