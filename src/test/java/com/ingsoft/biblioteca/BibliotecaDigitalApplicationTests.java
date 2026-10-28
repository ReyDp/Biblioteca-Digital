package com.ingsoft.biblioteca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.properties")
@Tag("postgres")
class BibliotecaDigitalApplicationTests {

	@Test
	void contextLoads() {
	}
}
