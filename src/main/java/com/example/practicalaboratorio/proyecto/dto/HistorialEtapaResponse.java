package com.example.practicalaboratorio.proyecto.dto;

import com.example.practicalaboratorio.proyecto.entity.EtapaProyecto;
import com.example.practicalaboratorio.proyecto.entity.HistorialEtapa;

import java.time.LocalDateTime;

public record HistorialEtapaResponse(
        Long id,
        EtapaProyecto etapaAnterior,
        EtapaProyecto etapaNueva,
        String usuarioNombre,
        String observacion,
        LocalDateTime fecha
) {
    public static HistorialEtapaResponse desde(HistorialEtapa historial) {
        return new HistorialEtapaResponse(
                historial.getId(),
                historial.getEtapaAnterior(),
                historial.getEtapaNueva(),
                historial.getUsuario().getNombre() + " " + historial.getUsuario().getApellido(),
                historial.getObservacion(),
                historial.getFecha()
        );
    }
}