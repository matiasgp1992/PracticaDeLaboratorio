package com.example.practicalaboratorio.proyecto.dto;

import com.example.practicalaboratorio.proyecto.entity.EtapaProyecto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEtapaRequest(
        @NotNull(message = "La etapa es obligatoria")
        EtapaProyecto etapa,

        @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
        String observacion
) {
}