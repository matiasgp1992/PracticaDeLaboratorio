package com.example.practicalaboratorio.auth.dto;

import com.example.practicalaboratorio.usuario.dto.UsuarioResponse;

public record AuthResponse(
        String token,
        UsuarioResponse usuario
) {
}