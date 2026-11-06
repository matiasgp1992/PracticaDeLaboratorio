package com.example.practicalaboratorio.proyecto.dto;

import com.example.practicalaboratorio.proyecto.entity.MiembroProyecto;
import com.example.practicalaboratorio.usuario.entity.Rol;
import com.example.practicalaboratorio.usuario.entity.Usuario;

import java.time.LocalDateTime;

public record MiembroResponse(
        Long usuarioId,
        String nombre,
        String apellido,
        String email,
        Rol rol,
        String funcion,
        LocalDateTime fechaAsignacion
) {
    public static MiembroResponse desde(MiembroProyecto miembro) {
        Usuario usuario = miembro.getUsuario();
        return new MiembroResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol(),
                miembro.getFuncion(),
                miembro.getFechaAsignacion()
        );
    }
}