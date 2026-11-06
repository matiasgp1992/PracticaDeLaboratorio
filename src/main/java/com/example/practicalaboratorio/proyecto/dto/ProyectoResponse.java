package com.example.practicalaboratorio.proyecto.dto;

import com.example.practicalaboratorio.proyecto.entity.EstadoProyecto;
import com.example.practicalaboratorio.proyecto.entity.EtapaProyecto;
import com.example.practicalaboratorio.proyecto.entity.Proyecto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProyectoResponse(
        Long id,
        String nombre,
        String direccionObra,
        Long clienteId,
        String clienteDenominacion,
        EtapaProyecto etapaActual,
        EstadoProyecto estado,
        LocalDate fechaInicio,
        LocalDate fechaFinEstimada,
        LocalDateTime fechaAlta
) {
    public static ProyectoResponse desde(Proyecto proyecto) {
        return new ProyectoResponse(
                proyecto.getId(),
                proyecto.getNombre(),
                proyecto.getDireccionObra(),
                proyecto.getCliente().getId(),
                proyecto.getCliente().getDenominacion(),
                proyecto.getEtapaActual(),
                proyecto.getEstado(),
                proyecto.getFechaInicio(),
                proyecto.getFechaFinEstimada(),
                proyecto.getFechaAlta()
        );
    }
}