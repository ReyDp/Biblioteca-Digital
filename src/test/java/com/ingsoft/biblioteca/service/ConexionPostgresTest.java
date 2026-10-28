package com.ingsoft.biblioteca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Tag("postgres")
class ConexionPostgresTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void seConectaExclusivamenteALaBaseDePruebas() {
        String baseActual = jdbcTemplate.queryForObject(
                "SELECT current_database()",
                String.class
        );

        assertEquals(
                "biblioteca_digital_test",
                baseActual,
                "Las pruebas deben usar exclusivamente la base de datos de pruebas."
        );
    }
}
