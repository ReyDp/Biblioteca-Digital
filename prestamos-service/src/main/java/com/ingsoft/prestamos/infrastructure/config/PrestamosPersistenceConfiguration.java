
package com.ingsoft.prestamos.infrastructure.config;

import com.ingsoft.prestamos.application.port.input.ConsultarPrestamoUseCase;
import com.ingsoft.prestamos.application.port.input.RegistrarDevolucionUseCase;
import com.ingsoft.prestamos.application.port.input.RegistrarPrestamoUseCase;
import com.ingsoft.prestamos.application.port.output.ClienteQueryPort;
import com.ingsoft.prestamos.application.port.output.PrestamoPersistencePort;
import com.ingsoft.prestamos.application.port.output.PublicarPrestamoDevueltoPort;
import com.ingsoft.prestamos.application.port.output.ReservaLibroPort;
import com.ingsoft.prestamos.application.service.ConsultarPrestamoService;
import com.ingsoft.prestamos.application.service.RegistrarDevolucionService;
import com.ingsoft.prestamos.application.service.RegistrarPrestamoService;

import java.time.Clock;
import java.time.ZoneOffset;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PrestamosPersistenceConfiguration {

    @Bean
    Clock applicationClock() {
        return Clock.system(ZoneOffset.UTC);
    }

    @Bean
    RegistrarPrestamoUseCase registrarPrestamoUseCase(
            PrestamoPersistencePort persistence,
            ClienteQueryPort clienteQuery,
            ReservaLibroPort reservaLibro,
            Clock clock) {

        return new RegistrarPrestamoService(
                persistence,
                clienteQuery,
                reservaLibro,
                clock
        );
    }

    @Bean
    RegistrarDevolucionUseCase registrarDevolucionUseCase(
            PrestamoPersistencePort persistence,
            PublicarPrestamoDevueltoPort publisher,
            Clock clock) {

        return new RegistrarDevolucionService(
                persistence,
                publisher,
                clock
        );
    }

    @Bean
    ConsultarPrestamoUseCase consultarPrestamoUseCase(
            PrestamoPersistencePort persistence) {

        return new ConsultarPrestamoService(persistence);
    }
}