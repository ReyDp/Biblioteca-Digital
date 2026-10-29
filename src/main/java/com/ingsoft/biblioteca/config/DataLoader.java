package com.ingsoft.biblioteca.config;

import com.ingsoft.biblioteca.entity.*;
import com.ingsoft.biblioteca.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@Profile("dev-data")
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner cargarDatosDePrueba(
            CategoriaRepository categoriaRepository,
            LibroRepository libroRepository,
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            PrestamoRepository prestamoRepository,
            MultaRepository multaRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            log.info("=== DataLoader: cargando datos de prueba (perfil dev-data) ===");

            // 1. Categoría para los libros nuevos
            Categoria ingenieriaSoftware = categoriaRepository
                    .findByNombreIgnoreCase("Ingeniería de software")
                    .orElseGet(() -> {
                        Categoria c = new Categoria();
                        c.setNombre("Ingeniería de software");
                        c.setDescripcion("Libros sobre ingeniería de software, diseño, patrones y prácticas de desarrollo.");
                        return categoriaRepository.save(c);
                    });

            // 2. Cinco libros nuevos (sin portada, porque no se verificaron URLs confiables)
            crearLibroSiNoExiste(libroRepository, ingenieriaSoftware,
                    "The Mythical Man-Month", "Frederick P. Brooks Jr.",
                    "Clásico sobre gestión de proyectos de software. Presenta la ley de Brooks: añadir personal a un proyecto retrasado lo retrasa aún más.");

            crearLibroSiNoExiste(libroRepository, ingenieriaSoftware,
                    "Head First Design Patterns", "Eric Freeman, Elisabeth Robson",
                    "Guía visual y práctica de patrones de diseño orientado a objetos. Enseña los patrones del libro GoF con ejemplos interactivos.");

            crearLibroSiNoExiste(libroRepository, ingenieriaSoftware,
                    "Effective Java", "Joshua Bloch",
                    "Recopilación de mejores prácticas para programación en Java. Cubre construcción de objetos, métodos, interfaces y genéricos.");

            crearLibroSiNoExiste(libroRepository, ingenieriaSoftware,
                    "Refactoring", "Martin Fowler",
                    "Guía completa para mejorar el diseño de código existente. Presenta técnicas sistemáticas de refactorización con ejemplos prácticos.");

            crearLibroSiNoExiste(libroRepository, ingenieriaSoftware,
                    "Spring Start Here", "Laurentiu Spilca",
                    "Introducción práctica al framework Spring Boot. Enseña a construir aplicaciones web modernas con Java y Spring.");

            // 3. Cliente de prueba
            String testCorreo = "test-prueba@biblioteca.local";
            String testDocumento = "TEST-001";

            Cliente clienteTest = clienteRepository.findByDocumento(testDocumento)
                    .orElseGet(() -> {
                        if (usuarioRepository.existsByCorreo(testCorreo)) {
                            log.warn("DataLoader: ya existe un usuario con {}", testCorreo);
                            throw new IllegalStateException("DataLoader: correo de prueba ya existe");
                        }
                        if (clienteRepository.existsByDocumento(testDocumento)) {
                            log.warn("DataLoader: ya existe un cliente con {}", testDocumento);
                            throw new IllegalStateException("DataLoader: documento de prueba ya existe");
                        }

                        Usuario usuario = new Usuario();
                        usuario.setNombre("Cliente de prueba");
                        usuario.setCorreo(testCorreo);
                        usuario.setPasswordHash(passwordEncoder.encode("test1234"));
                        usuario.setRol(RolUsuario.CLIENTE);
                        usuario.setActivo(true);
                        usuario = usuarioRepository.save(usuario);

                        Cliente cliente = new Cliente();
                        cliente.setUsuario(usuario);
                        cliente.setDocumento(testDocumento);
                        cliente.setTelefono("3001234567");
                        return clienteRepository.save(cliente);
                    });

            // 4. Dos ejemplares físicos para los escenarios de préstamo
            Libro libroVencido = crearLibroSiNoExiste(libroRepository, ingenieriaSoftware,
                    "Libro de prueba — Préstamo vencido", "Autor de prueba",
                    "Ejemplar dedicado al escenario de préstamo vencido activo con multa estimada.");

            Libro libroMulta = crearLibroSiNoExiste(libroRepository, ingenieriaSoftware,
                    "Libro de prueba — Multa pendiente", "Autor de prueba",
                    "Ejemplar dedicado al escenario de préstamo devuelto con multa definitiva pendiente.");

            // 5. Escenario A: Préstamo vencido activo (sin multa definitiva)
            LocalDate hoy = LocalDate.now();
            boolean yaTienePrestamoActivoVencido = prestamoRepository
                    .existsByLibroIdAndEstado(libroVencido.getId(), EstadoPrestamo.ACTIVO);

            if (!yaTienePrestamoActivoVencido) {
                Prestamo prestamoVencido = new Prestamo();
                prestamoVencido.setCliente(clienteTest);
                prestamoVencido.setLibro(libroVencido);
                prestamoVencido.setFechaPrestamo(hoy.minusDays(20));
                prestamoVencido.setFechaLimite(hoy.minusDays(5));
                prestamoVencido.setEstado(EstadoPrestamo.ACTIVO);
                prestamoRepository.save(prestamoVencido);
                log.info("DataLoader: préstamo vencido activo creado (límite: {})", hoy.minusDays(5));
            } else {
                log.info("DataLoader: préstamo vencido activo ya existe para el libro {}", libroVencido.getId());
            }

            // 6. Escenario B: Préstamo devuelto con multa pendiente
            boolean yaTienePrestamoMulta = prestamoRepository
                    .existsByLibroIdAndEstado(libroMulta.getId(), EstadoPrestamo.DEVUELTO);

            if (!yaTienePrestamoMulta) {
                Prestamo prestamoMulta = new Prestamo();
                prestamoMulta.setCliente(clienteTest);
                prestamoMulta.setLibro(libroMulta);
                prestamoMulta.setFechaPrestamo(hoy.minusDays(25));
                prestamoMulta.setFechaLimite(hoy.minusDays(10));
                prestamoMulta.setFechaDevolucion(hoy.minusDays(7));
                prestamoMulta.setEstado(EstadoPrestamo.DEVUELTO);
                prestamoMulta = prestamoRepository.save(prestamoMulta);

                // Multa: 3 días de retraso × $1.000 = $3.000
                Multa multa = new Multa();
                multa.setPrestamo(prestamoMulta);
                multa.setTarifaDiaria(new BigDecimal("1000.00"));
                multa.setDiasRetrasoFinales(3);
                multa.setImporteTotal(new BigDecimal("3000.00"));
                multa.setFechaCalculo(hoy.minusDays(7));
                multa.setEstado(EstadoMulta.PENDIENTE);
                multaRepository.save(multa);
                log.info("DataLoader: préstamo devuelto con multa pendiente de $3.000 creado");
            } else {
                log.info("DataLoader: préstamo devuelto con multa ya existe para el libro {}", libroMulta.getId());
            }

            log.info("=== DataLoader: datos de prueba cargados correctamente ===");
        };
    }

    private Libro crearLibroSiNoExiste(LibroRepository repo, Categoria categoria,
                                       String titulo, String autor, String descripcion) {
        // Verificar duplicado buscando por título y autor
        boolean existe = repo.findByTituloContainingIgnoreCaseAndActivoTrue(titulo)
                .stream()
                .anyMatch(l -> l.getAutor().equals(autor));

        if (existe) {
            log.info("DataLoader: libro '{}' ya existe", titulo);
            return repo.findByTituloContainingIgnoreCaseAndActivoTrue(titulo)
                    .stream()
                    .filter(l -> l.getAutor().equals(autor))
                    .findFirst()
                    .orElseThrow();
        }

        Libro libro = new Libro();
        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setDescripcion(descripcion);
        libro.setCategoria(categoria);
        libro.setActivo(true);
        // portadaUrl = null — el usuario debe agregarla desde el panel de administración
        Libro guardado = repo.save(libro);
        log.info("DataLoader: libro '{}' creado", titulo);
        return guardado;
    }
}
