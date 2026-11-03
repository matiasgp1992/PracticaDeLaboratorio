package com.example.practicalaboratorio.usuario.controller;

import com.example.practicalaboratorio.auth.security.UsuarioAutenticado;
import com.example.practicalaboratorio.usuario.dto.CambiarEstadoRequest;
import com.example.practicalaboratorio.usuario.dto.CrearUsuarioRequest;
import com.example.practicalaboratorio.usuario.dto.UsuarioResponse;
import com.example.practicalaboratorio.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public UsuarioResponse yo(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return usuarioService.obtenerActual(actual);
    }

    @GetMapping
    public List<UsuarioResponse> listar(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return usuarioService.listar(actual);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody CrearUsuarioRequest request,
                                 @AuthenticationPrincipal UsuarioAutenticado actual) {
        return usuarioService.crear(request, actual);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse cambiarEstado(@PathVariable Long id,
                                         @Valid @RequestBody CambiarEstadoRequest request,
                                         @AuthenticationPrincipal UsuarioAutenticado actual) {
        return usuarioService.cambiarEstado(id, request.activo(), actual);
    }
}