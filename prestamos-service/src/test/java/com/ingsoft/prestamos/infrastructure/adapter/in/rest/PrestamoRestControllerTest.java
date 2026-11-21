
package com.ingsoft.prestamos.infrastructure.adapter.in.rest;

import com.ingsoft.prestamos.application.port.input.RegistrarDevolucionUseCase;
import com.ingsoft.prestamos.application.port.input.RegistrarPrestamoUseCase;
import com.ingsoft.prestamos.application.port.output.IdempotenciaPort;
import com.ingsoft.prestamos.domain.exception.ClienteNoExisteException;
import com.ingsoft.prestamos.domain.exception.LibroNoDisponibleException;
import com.ingsoft.prestamos.domain.exception.PrestamoNoExisteException;
import com.ingsoft.prestamos.domain.exception.PrestamoYaDevueltoException;
import com.ingsoft.prestamos.domain.model.ClienteId;
import com.ingsoft.prestamos.domain.model.EstadoPrestamo;
import com.ingsoft.prestamos.domain.model.LibroId;
import com.ingsoft.prestamos.domain.model.Prestamo;
import com.ingsoft.prestamos.domain.model.PrestamoId;
import com.ingsoft.prestamos.domain.model.ReservaId;
import com.ingsoft.prestamos.infrastructure.adapter.output.persistence.JpaIdempotenciaAdapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PrestamoRestControllerTest {

    private static final String BASE_URL = "/api/prestamos";
    private static final String IDEMPOTENCY_KEY =
            "test-idempotency-key-001";

    private static final Long CLIENTE_ID = 10L;
    private static final Long LIBRO_ID = 20L;
    private static final Long PRESTAMO_ID = 100L;

    private static final LocalDate FECHA_PRESTAMO =
            LocalDate.of(2026, 11, 18);

    private static final String RESPUESTA_SERIALIZADA =
            "{\"id\":100,\"clienteId\":10,\"libroId\":20,"
                    + "\"fechaPrestamo\":\"2026-11-18\","
                    + "\"fechaLimite\":\"2026-12-03\","
                    + "\"fechaDevolucion\":null,\"estado\":\"ACTIVO\"}";

    @Mock
    private RegistrarPrestamoUseCase registrarPrestamoUseCase;

    @Mock
    private RegistrarDevolucionUseCase registrarDevolucionUseCase;

    @Mock
    private IdempotenciaPort idempotenciaPort;

    @Mock
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() throws Exception {
        PrestamoRestController controller =
                new PrestamoRestController(
                        registrarPrestamoUseCase,
                        registrarDevolucionUseCase,
                        idempotenciaPort,
                        objectMapper
                );

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new PrestamoRestExceptionHandler())
                .setValidator(validator)
                .build();

        lenient().when(objectMapper.writeValueAsString(any()))
                .thenReturn(RESPUESTA_SERIALIZADA);
    }

    @Test
    void registrarPrestamo_rechazaLibroNoDisponible() throws Exception {
        prepararIdempotenciaPrestamo();

        when(registrarPrestamoUseCase.registrar(
                new ClienteId(CLIENTE_ID),
                new LibroId(LIBRO_ID)
        )).thenThrow(new LibroNoDisponibleException());

        mockMvc.perform(post(BASE_URL)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":10,\"libroId\":20}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("LIBRO_NO_DISPONIBLE"));

        verify(registrarPrestamoUseCase).registrar(
                new ClienteId(CLIENTE_ID),
                new LibroId(LIBRO_ID)
        );
    }

    @Test
    void registrarDevolucion_idempotencia_operacionEnProceso()
            throws Exception {

        String hash = JpaIdempotenciaAdapter.computeHash(
                String.valueOf(PRESTAMO_ID)
        );

        when(idempotenciaPort.buscar(
                eq("registrar_devolucion"),
                eq(IDEMPOTENCY_KEY),
                eq(hash)
        )).thenReturn(Optional.of(
                new IdempotenciaPort.Resultado(
                        hash,
                        null,
                        IdempotenciaPort.Resultado.Estado.EN_PROCESO
                )
        ));

        mockMvc.perform(post(
                        BASE_URL + "/{id}/devolucion",
                        PRESTAMO_ID
                )
                        .header("Idempotency-Key", IDEMPOTENCY_KEY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("IDEMPOTENCIA_CONFLICTO"));

        verify(registrarDevolucionUseCase, never())
                .registrar(any());
    }

    @Test
    void registrarPrestamo_rechazaSolicitudInvalida()
            throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":0,\"libroId\":20}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDACION_FALLIDA"));

        verifyNoInteractions(registrarPrestamoUseCase);
    }

    @Test
    void registrarPrestamo_rechazaSinIdempotencyKey()
            throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":10,\"libroId\":20}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registrarPrestamoUseCase);
        verifyNoInteractions(idempotenciaPort);
    }

    @Test
    void registrarPrestamo_confirmaPrestamoCorrectamente()
            throws Exception {

        prepararIdempotenciaPrestamo();

        Prestamo prestamo = crearPrestamoActivo();

        when(registrarPrestamoUseCase.registrar(
                new ClienteId(CLIENTE_ID),
                new LibroId(LIBRO_ID)
        )).thenReturn(prestamo);

        mockMvc.perform(post(BASE_URL)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":10,\"libroId\":20}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(PRESTAMO_ID))
                .andExpect(jsonPath("$.clienteId").value(CLIENTE_ID))
                .andExpect(jsonPath("$.libroId").value(LIBRO_ID))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        verify(idempotenciaPort).registrarEnProceso(
                eq("registrar_prestamo"),
                eq(IDEMPOTENCY_KEY),
                anyString()
        );

        verify(idempotenciaPort).completar(
                eq("registrar_prestamo"),
                eq(IDEMPOTENCY_KEY),
                anyString(),
                anyString()
        );
    }

    @Test
    void registrarDevolucion_rechazaSinIdempotencyKey()
            throws Exception {

        mockMvc.perform(post(
                        BASE_URL + "/{id}/devolucion",
                        PRESTAMO_ID
                ))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registrarDevolucionUseCase);
        verifyNoInteractions(idempotenciaPort);
    }

    @Test
    void registrarDevolucion_creaEventoOutbox()
            throws Exception {

        prepararIdempotenciaDevolucion();

        Prestamo prestamo = crearPrestamoDevuelto();

        when(registrarDevolucionUseCase.registrar(
                new PrestamoId(PRESTAMO_ID)
        )).thenReturn(prestamo);

        mockMvc.perform(post(
                        BASE_URL + "/{id}/devolucion",
                        PRESTAMO_ID
                )
                        .header("Idempotency-Key", IDEMPOTENCY_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRESTAMO_ID))
                .andExpect(jsonPath("$.estado").value("DEVUELTO"));

        verify(registrarDevolucionUseCase).registrar(
                new PrestamoId(PRESTAMO_ID)
        );

        verify(idempotenciaPort).completar(
                eq("registrar_devolucion"),
                eq(IDEMPOTENCY_KEY),
                anyString(),
                anyString()
        );
    }

    @Test
    void registrarPrestamo_idempotencia_operacionEnProceso()
            throws Exception {

        String hash = JpaIdempotenciaAdapter.computeHash(
                CLIENTE_ID + ":" + LIBRO_ID
        );

        when(idempotenciaPort.buscar(
                eq("registrar_prestamo"),
                eq(IDEMPOTENCY_KEY),
                eq(hash)
        )).thenReturn(Optional.of(
                new IdempotenciaPort.Resultado(
                        hash,
                        null,
                        IdempotenciaPort.Resultado.Estado.EN_PROCESO
                )
        ));

        mockMvc.perform(post(BASE_URL)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":10,\"libroId\":20}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("IDEMPOTENCIA_CONFLICTO"));

        verifyNoInteractions(registrarPrestamoUseCase);
    }

    @Test
    void registrarPrestamo_rechazaClienteInexistente()
            throws Exception {

        prepararIdempotenciaPrestamo();

        when(registrarPrestamoUseCase.registrar(
                new ClienteId(CLIENTE_ID),
                new LibroId(LIBRO_ID)
        )).thenThrow(new ClienteNoExisteException());

        mockMvc.perform(post(BASE_URL)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":10,\"libroId\":20}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("CLIENTE_NO_ENCONTRADO"));
    }

    @Test
    void registrarPrestamo_rechazaJsonInvalido()
            throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("JSON_INVALIDO"));

        verifyNoInteractions(registrarPrestamoUseCase);
    }

    @Test
    void registrarPrestamo_exitoso() throws Exception {
        prepararIdempotenciaPrestamo();

        when(registrarPrestamoUseCase.registrar(
                new ClienteId(CLIENTE_ID),
                new LibroId(LIBRO_ID)
        )).thenReturn(crearPrestamoActivo());

        mockMvc.perform(post(BASE_URL)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":10,\"libroId\":20}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(PRESTAMO_ID))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        verify(registrarPrestamoUseCase).registrar(
                new ClienteId(CLIENTE_ID),
                new LibroId(LIBRO_ID)
        );
    }

    @Test
    void registrarPrestamo_idempotencia_conflictConContenidoDiferente()
            throws Exception {

        when(idempotenciaPort.buscar(
                eq("registrar_prestamo"),
                eq(IDEMPOTENCY_KEY),
                anyString()
        )).thenReturn(Optional.of(
                new IdempotenciaPort.Resultado(
                        "hash-diferente",
                        null,
                        IdempotenciaPort.Resultado.Estado.EN_PROCESO
                )
        ));

        mockMvc.perform(post(BASE_URL)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":10,\"libroId\":20}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("IDEMPOTENCIA_CONFLICTO"));

        verifyNoInteractions(registrarPrestamoUseCase);
    }

    @Test
    void registrarDevolucion_exitoso() throws Exception {
        prepararIdempotenciaDevolucion();

        when(registrarDevolucionUseCase.registrar(
                new PrestamoId(PRESTAMO_ID)
        )).thenReturn(crearPrestamoDevuelto());

        mockMvc.perform(post(
                        BASE_URL + "/{id}/devolucion",
                        PRESTAMO_ID
                )
                        .header("Idempotency-Key", IDEMPOTENCY_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRESTAMO_ID))
                .andExpect(jsonPath("$.estado").value("DEVUELTO"));

        verify(registrarDevolucionUseCase).registrar(
                new PrestamoId(PRESTAMO_ID)
        );
    }

    @Test
    void registrarDevolucion_rechazaPrestamoInexistente()
            throws Exception {

        prepararIdempotenciaDevolucion();

        when(registrarDevolucionUseCase.registrar(
                new PrestamoId(PRESTAMO_ID)
        )).thenThrow(new PrestamoNoExisteException());

        mockMvc.perform(post(
                        BASE_URL + "/{id}/devolucion",
                        PRESTAMO_ID
                )
                        .header("Idempotency-Key", IDEMPOTENCY_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("PRESTAMO_NO_ENCONTRADO"));
    }

    @Test
    void registrarDevolucion_rechazaPrestamoYaDevuelto()
            throws Exception {

        prepararIdempotenciaDevolucion();

        when(registrarDevolucionUseCase.registrar(
                new PrestamoId(PRESTAMO_ID)
        )).thenThrow(new PrestamoYaDevueltoException());

        mockMvc.perform(post(
                        BASE_URL + "/{id}/devolucion",
                        PRESTAMO_ID
                )
                        .header("Idempotency-Key", IDEMPOTENCY_KEY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("PRESTAMO_YA_DEVUELTO"));
    }

    private void prepararIdempotenciaPrestamo() {
        when(idempotenciaPort.buscar(
                eq("registrar_prestamo"),
                eq(IDEMPOTENCY_KEY),
                anyString()
        )).thenReturn(Optional.empty());
    }

    private void prepararIdempotenciaDevolucion() {
        when(idempotenciaPort.buscar(
                eq("registrar_devolucion"),
                eq(IDEMPOTENCY_KEY),
                anyString()
        )).thenReturn(Optional.empty());
    }

    private Prestamo crearPrestamoActivo() {
        Prestamo prestamo = Prestamo.crearPendiente(
                new ClienteId(CLIENTE_ID),
                new LibroId(LIBRO_ID),
                new ReservaId("RES-001"),
                FECHA_PRESTAMO
        );

        prestamo.confirmarReserva();

        return prestamo.conId(new PrestamoId(PRESTAMO_ID));
    }

    private Prestamo crearPrestamoDevuelto() {
        return Prestamo.rehidratar(
                new PrestamoId(PRESTAMO_ID),
                new ClienteId(CLIENTE_ID),
                new LibroId(LIBRO_ID),
                new ReservaId("RES-001"),
                FECHA_PRESTAMO,
                FECHA_PRESTAMO.plusDays(15),
                FECHA_PRESTAMO.plusDays(5),
                EstadoPrestamo.DEVUELTO
        );
    }
}
