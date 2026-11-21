
package com.ingsoft.prestamos.infrastructure.config;

import com.ingsoft.prestamos.application.port.output.ClienteQueryPort;
import com.ingsoft.prestamos.application.port.output.ReservaLibroPort;
import com.ingsoft.prestamos.infrastructure.adapter.output.http.BibliotecaDigitalHttpClient;
import com.ingsoft.prestamos.infrastructure.adapter.output.http.HttpClienteQueryAdapter;
import com.ingsoft.prestamos.infrastructure.adapter.output.http.HttpReservaLibroAdapter;
import com.ingsoft.prestamos.infrastructure.security.BibliotecaDigitalServiceJwtProperties;
import com.ingsoft.prestamos.infrastructure.security.BibliotecaDigitalServiceJwtTokenProvider;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BibliotecaDigitalServiceJwtProperties.class)
@ConditionalOnExpression(value="'${biblioteca.digital.url:}'.trim().length() > 0 and '${biblioteca.digital.service-jwt.private-key-location:}'.trim().length() > 0")
public class BibliotecaDigitalHttpConfiguration {
    @Value(value="${biblioteca.digital.url}")
    private String bibliotecaDigitalUrl;

    @Bean
    BibliotecaDigitalServiceJwtTokenProvider bibliotecaDigitalServiceJwtTokenProvider(BibliotecaDigitalServiceJwtProperties properties, Clock clock) {
        return new BibliotecaDigitalServiceJwtTokenProvider(properties, clock);
    }

    @Bean
    BibliotecaDigitalHttpClient bibliotecaDigitalHttpClient(BibliotecaDigitalServiceJwtTokenProvider tokenProvider) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
        return new BibliotecaDigitalHttpClient(client, this.bibliotecaDigitalUrl, tokenProvider);
    }

    @Bean
    ClienteQueryPort clienteQueryPort(BibliotecaDigitalHttpClient httpClient) {
        return new HttpClienteQueryAdapter(httpClient);
    }

    @Bean
    ReservaLibroPort reservaLibroPort(BibliotecaDigitalHttpClient httpClient) {
        return new HttpReservaLibroAdapter(httpClient);
    }
}
