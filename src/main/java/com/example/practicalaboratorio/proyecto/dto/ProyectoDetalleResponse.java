package com.example.practicalaboratorio.proyecto.dto;

import com.example.practicalaboratorio.proyecto.entity.EstadoProyecto;
import com.example.practicalaboratorio.proyecto.entity.EtapaProyecto;
import com.example.practicalaboratorio.proyecto.entity.Proyecto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProyectoDetalleResponse(
        Long id,
        String nombre,
        String descripcion,
        String direccionObra,
        Long clienteId,
        String clienteDenominacion,
        boolean clienteActivo,
        EtapaProyecto etapaActual,
        EstadoProyecto estado,
        LocalDate fechaInicio,
        LocalDate fechaFinEstimada,
        LocalDateTime fechaAlta,
        List<MiembroResponse> miembros,
        List<HistorialEtapaResponse> historial
) {
    public static ProyectoDetalleResponse desde(Proyecto proyecto,
                                                List<MiembroResponse> miembros,
                                                List<HistorialEtapaResponse> historial) {
        return new ProyectoDetalleResponse(
                proyecto.getId(),
                proyecto.getNombre(),
                proyecto.getDescripcion(),
                proyecto.getDireccionObra(),
                proyecto.getCliente().getId(),
                proyecto.getCliente().getDenominacion(),
                proyecto.getCliente().isActivo(),
                proyecto.getEtapaActual(),
                proyecto.getEstado(),
                proyecto.getFechaInicio(),
                proyecto.getFechaFinEstimada(),
                proyecto.getFechaAlta(),
                miembros,
                historial
        );
    }
}