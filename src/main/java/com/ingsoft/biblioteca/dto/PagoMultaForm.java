package com.ingsoft.biblioteca.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PagoMultaForm {

    @NotNull(message = "El valor es obligatorio.")
    @DecimalMin(value = "0.01", message = "El valor debe ser mayor que cero.")
    private BigDecimal valor;

    @NotBlank(message = "El método de pago es obligatorio.")
    @Size(max = 40, message = "El método no puede superar 40 caracteres.")
    private String metodo;

    @Size(max = 100, message = "La referencia no puede superar 100 caracteres.")
    private String referencia;

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}
