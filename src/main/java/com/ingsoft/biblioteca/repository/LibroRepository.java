package com.ingsoft.biblioteca.repository;

import com.ingsoft.biblioteca.entity.Libro;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    List<Libro> findByActivoTrue();

    List<Libro> findByTituloContainingIgnoreCaseAndActivoTrue(
            String titulo
    );

    List<Libro> findByAutorContainingIgnoreCaseAndActivoTrue(
            String autor
    );

    List<Libro> findByCategoriaIdAndActivoTrue(Long categoriaId);

    @Query("""
            SELECT l
            FROM Libro l
            JOIN FETCH l.categoria
            WHERE l.id = :id
            """)
    Optional<Libro> buscarPorIdConCategoria(@Param("id") Long id);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT l 
            FROM Libro l 
            WHERE l.id = :id""")
    Optional<Libro> buscarPorIdConBloqueo(@Param("id") Long id);

    @Query("""
    SELECT l
    FROM Libro l
    JOIN FETCH l.categoria c
    WHERE l.activo = true
      AND (
          :texto = ''
          OR LOWER(l.titulo) LIKE CONCAT('%', LOWER(:texto), '%')
          OR LOWER(l.autor) LIKE CONCAT('%', LOWER(:texto), '%')
      )
      AND (
          :categoriaId = 0
          OR l.categoria.id = :categoriaId
      )
    ORDER BY l.titulo ASC
    """)
    List<Libro> buscarCatalogo(
            @Param("texto") String texto,
            @Param("categoriaId") Long categoriaId
    );

}