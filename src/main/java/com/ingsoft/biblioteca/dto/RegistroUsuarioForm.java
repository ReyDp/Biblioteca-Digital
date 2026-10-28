package com.ingsoft.biblioteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistroUsuarioForm {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo electrónico válido.")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres.")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
    private String password;

    @NotBlank(message = "Confirma la contraseña.")
    private String confirmacionPassword;

    @NotBlank(message = "El documento es obligatorio.")
    @Size(max = 30, message = "El documento no puede superar 30 caracteres.")
    @Pattern(regexp = "^[A-Za-z0-9.-]+$", message = "El documento contiene caracteres no permitidos.")
    private String documento;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres.")
    @Pattern(regexp = "^$|^[0-9+() -]+$", message = "El teléfono contiene caracteres no permitidos.")
    private String telefono;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmacionPassword() { return confirmacionPassword; }
    public void setConfirmacionPassword(String confirmacionPassword) { this.confirmacionPassword = confirmacionPassword; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
