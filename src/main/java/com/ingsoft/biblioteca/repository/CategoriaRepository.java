package com.ingsoft.biblioteca.repository;

import com.ingsoft.biblioteca.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository
        extends  JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    java.util.List<Categoria> findAllByOrderByNombreAsc();
}
