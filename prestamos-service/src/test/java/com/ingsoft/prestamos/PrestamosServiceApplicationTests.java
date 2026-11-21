package com.ingsoft.prestamos;

import com.ingsoft.prestamos.application.port.input.RegistrarDevolucionUseCase;
import com.ingsoft.prestamos.application.port.input.RegistrarPrestamoUseCase;
import com.ingsoft.prestamos.application.port.output.IdempotenciaPort;
import com.ingsoft.prestamos.application.port.output.PrestamoPersistencePort;
import com.ingsoft.prestamos.infrastructure.security.ServiceJwtTokenProvider;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.OutboxEventRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PrestamosServiceApplicationTests {

    @Value("${spring.application.name}")
    private String applicationName;

    @MockitoBean
    private RegistrarPrestamoUseCase registrarPrestamoUseCase;

    @MockitoBean
    private RegistrarDevolucionUseCase registrarDevolucionUseCase;

    @MockitoBean
    private IdempotenciaPort idempotenciaPort;

    @MockitoBean
    private ServiceJwtTokenProvider serviceJwtTokenProvider;

    // Mock adicional para permitir cargar el contexto sin JPA.
    @MockitoBean
    private PrestamoPersistencePort prestamoPersistencePort;

    @MockitoBean
    private OutboxEventRepository outboxEventRepository;

    @Test
    void cargaElContextoIndependiente() {
        assertThat(applicationName).isEqualTo("prestamos-service");
    }

    @Test
    void usaElPerfilDePruebas() {
        assertThat(
                System.getProperty("spring.profiles.active", "test")
        ).isNotBlank();
    }
}
