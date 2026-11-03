package com.example.practicalaboratorio.usuario.dto;

import com.example.practicalaboratorio.usuario.entity.Rol;
import com.example.practicalaboratorio.usuario.entity.Usuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        Rol rol,
        Long organizacionId,
        String organizacionNombre,
        boolean activo
) {
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getOrganizacion().getId(),
                usuario.getOrganizacion().getNombre(),
                usuario.isActivo()
        );
    }
}