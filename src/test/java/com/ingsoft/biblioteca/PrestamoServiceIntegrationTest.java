
package com.ingsoft.biblioteca;

import com.ingsoft.biblioteca.entity.*;
import com.ingsoft.biblioteca.repository.*;
import com.ingsoft.biblioteca.service.PrestamoService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@Tag("postgres")
class PrestamoServiceIntegrationTest {

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private MultaRepository multaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void devolucionTardiaGuardaPrestamoYMultaEnPostgres() {
        String baseActual = jdbcTemplate.queryForObject(
                "SELECT current_database()",
                String.class
        );

        assertEquals("biblioteca_digital_test", baseActual);

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Integración");
        usuario.setCorreo("integracion-" + System.nanoTime() + "@test.com");
        usuario.setPasswordHash("hash-prueba");
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setActivo(true);
        usuario = usuarioRepository.saveAndFlush(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setDocumento("DOC-" + System.nanoTime());
        cliente.setTelefono("3000000000");
        cliente = clienteRepository.saveAndFlush(cliente);

        Categoria categoria = new Categoria();
        categoria.setNombre("Categoria " + System.nanoTime());
        categoria.setDescripcion("Categoría temporal de integración");
        categoria = categoriaRepository.saveAndFlush(categoria);

        Libro libro = new Libro();
        libro.setTitulo("Libro de integración");
        libro.setAutor("Autor de prueba");
        libro.setDescripcion("Registro temporal para probar devoluciones");
        libro.setCategoria(categoria);
        libro.setActivo(true);
        libro = libroRepository.saveAndFlush(libro);

        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(LocalDate.now().minusDays(18));
        prestamo.setFechaLimite(LocalDate.now().minusDays(3));
        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        prestamo = prestamoRepository.saveAndFlush(prestamo);

        Long prestamoId = prestamo.getId();

        prestamoService.registrarDevolucion(prestamoId);
        prestamoRepository.flush();
        multaRepository.flush();

        Prestamo prestamoGuardado = prestamoRepository.findById(prestamoId)
                .orElseThrow();

        Multa multaGuardada = multaRepository.findByPrestamoId(prestamoId)
                .orElseThrow();

        assertEquals(EstadoPrestamo.DEVUELTO, prestamoGuardado.getEstado());
        assertEquals(LocalDate.now(), prestamoGuardado.getFechaDevolucion());

        assertEquals(3, multaGuardada.getDiasRetrasoFinales());
        assertEquals(
                0,
                new BigDecimal("3000.00")
                        .compareTo(multaGuardada.getImporteTotal())
        );
        assertEquals(
                LocalDate.now().minusDays(3),
                prestamoGuardado.getFechaLimite()
        );
        assertEquals(EstadoMulta.PENDIENTE, multaGuardada.getEstado());
        assertEquals(prestamoId, multaGuardada.getPrestamo().getId());
    }
}
