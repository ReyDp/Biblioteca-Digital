
package com.ingsoft.prestamos.infrastructure.config;

import com.ingsoft.prestamos.application.port.output.ClienteQueryPort;
import com.ingsoft.prestamos.application.port.output.ReservaLibroPort;
import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import com.ingsoft.prestamos.domain.model.ReservaLibro;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression(
    value = "'${biblioteca.digital.url:}'.trim().length() == 0 or '${biblioteca.digital.service-jwt.private-key-location:}'.trim().length() == 0"
)
public class BibliotecaDigitalFallbackConfiguration {

    @Bean
    ClienteQueryPort clienteQueryPort() {
        return new ClienteQueryPortStub();
    }

    @Bean
    ReservaLibroPort reservaLibroPort() {
        return new ReservaLibroPortStub();
    }

    private static class ClienteQueryPortStub implements ClienteQueryPort {

        @Override
        public boolean existe(ClienteId clienteId) {
            throw new IllegalStateException(
                "biblioteca.digital.url no está configurado. El servicio no puede verificar la existencia de clientes."
            );
        }
    }

    private static class ReservaLibroPortStub implements ReservaLibroPort {

        @Override
        public ReservaLibro solicitarReserva(LibroId libroId) {
            throw new IllegalStateException(
                "biblioteca.digital.url no está configurado. El servicio no puede solicitar reservas de libros."
            );
        }

        @Override
        public Optional<ReservaLibro> consultarReserva(ReservaId reservaId) {
            throw new IllegalStateException(
                "biblioteca.digital.url no está configurado. El servicio no puede consultar reservas."
            );
        }

        @Override
        public ReservaLibro confirmarReserva(ReservaId reservaId) {
            throw new IllegalStateException(
                "biblioteca.digital.url no está configurado. El servicio no puede confirmar reservas."
            );
        }

        @Override
        public ReservaLibro cancelarReserva(ReservaId reservaId) {
            throw new IllegalStateException(
                "biblioteca.digital.url no está configurado. El servicio no puede cancelar reservas."
            );
        }
    }
}
