package com.example.practicalaboratorio.proyecto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AgregarMiembroRequest(
        @NotNull(message = "El usuario es obligatorio")
        Long usuarioId,

        @NotBlank(message = "La función es obligatoria")
        @Size(max = 100, message = "La función no puede superar los 100 caracteres")
        String funcion
) {
}