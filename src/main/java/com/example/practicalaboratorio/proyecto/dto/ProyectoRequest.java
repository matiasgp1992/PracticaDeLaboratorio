package com.example.practicalaboratorio.proyecto.dto;

import com.example.practicalaboratorio.proyecto.entity.EstadoProyecto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProyectoRequest(
        @NotBlank(message = "El nombre del proyecto es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,

        @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
        String descripcion,

        @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
        String direccionObra,

        @NotNull(message = "El cliente es obligatorio")
        Long clienteId,

        LocalDate fechaInicio,

        LocalDate fechaFinEstimada,

        EstadoProyecto estado
) {
}