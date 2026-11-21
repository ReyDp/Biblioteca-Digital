
package com.ingsoft.prestamos.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegistrarPrestamoRequest(@NotNull(message="El cliente es obligatorio.") @Positive(message="El cliente debe ser un identificador v\u00e1lido.") @NotNull(message="El cliente es obligatorio.") @Positive(message="El cliente debe ser un identificador v\u00e1lido.") Long clienteId, @NotNull(message="El libro es obligatorio.") @Positive(message="El libro debe ser un identificador v\u00e1lido.") @NotNull(message="El libro es obligatorio.") @Positive(message="El libro debe ser un identificador v\u00e1lido.") Long libroId) {
}

