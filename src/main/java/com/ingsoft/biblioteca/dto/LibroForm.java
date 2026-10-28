package com.ingsoft.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LibroForm {

    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 200, message = "El título no puede superar 200 caracteres.")
    private String titulo;

    @NotBlank(message = "El autor es obligatorio.")
    @Size(max = 150, message = "El autor no puede superar 150 caracteres.")
    private String autor;

    private String descripcion;

    @Size(max = 500, message = "La URL de portada no puede superar 500 caracteres.")
    private String portadaUrl;

    @Size(max = 1000, message = "La URL del archivo no puede superar 1000 caracteres.")
    private String archivoUrl;

    @NotNull(message = "Selecciona una categoría.")
    private Long categoriaId;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getPortadaUrl() { return portadaUrl; }
    public void setPortadaUrl(String portadaUrl) { this.portadaUrl = portadaUrl; }
    public String getArchivoUrl() { return archivoUrl; }
    public void setArchivoUrl(String archivoUrl) { this.archivoUrl = archivoUrl; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
}
