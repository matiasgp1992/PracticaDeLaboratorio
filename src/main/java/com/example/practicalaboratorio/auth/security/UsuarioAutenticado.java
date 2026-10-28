package com.example.practicalaboratorio.auth.security;

import com.example.practicalaboratorio.usuario.entity.Rol;

public record UsuarioAutenticado(
        Long id,
        String email,
        Long organizacionId,
        Rol rol
) {
}