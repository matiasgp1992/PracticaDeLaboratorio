package com.example.practicalaboratorio.proyecto.controller;

import com.example.practicalaboratorio.auth.security.UsuarioAutenticado;
import com.example.practicalaboratorio.common.dto.PaginaResponse;
import com.example.practicalaboratorio.proyecto.dto.*;
import com.example.practicalaboratorio.proyecto.entity.EstadoProyecto;
import com.example.practicalaboratorio.proyecto.entity.EtapaProyecto;
import com.example.practicalaboratorio.proyecto.service.ProyectoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService proyectoService;

    @GetMapping
    public PaginaResponse<ProyectoResponse> buscar(@RequestParam(required = false) String texto,
                                                   @RequestParam(required = false) Long clienteId,
                                                   @RequestParam(required = false) EtapaProyecto etapa,
                                                   @RequestParam(required = false) EstadoProyecto estado,
                                                   @RequestParam(defaultValue = "0") int pagina,
                                                   @RequestParam(defaultValue = "10") int tamanio,
                                                   @AuthenticationPrincipal UsuarioAutenticado actual) {
        return proyectoService.buscar(texto, clienteId, etapa, estado, pagina, tamanio, actual);
    }

    @GetMapping("/{id}")
    public ProyectoDetalleResponse obtener(@PathVariable Long id,
                                           @AuthenticationPrincipal UsuarioAutenticado actual) {
        return proyectoService.obtener(id, actual);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ARQUITECTO')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProyectoDetalleResponse crear(@Valid @RequestBody ProyectoRequest request,
                                         @AuthenticationPrincipal UsuarioAutenticado actual) {
        return proyectoService.crear(request, actual);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ARQUITECTO')")
    public ProyectoDetalleResponse actualizar(@PathVariable Long id,
                                              @Valid @RequestBody ProyectoRequest request,
                                              @AuthenticationPrincipal UsuarioAutenticado actual) {
        return proyectoService.actualizar(id, request, actual);
    }

    @PatchMapping("/{id}/etapa")
    public ProyectoDetalleResponse cambiarEtapa(@PathVariable Long id,
                                                @Valid @RequestBody CambiarEtapaRequest request,
                                                @AuthenticationPrincipal UsuarioAutenticado actual) {
        return proyectoService.cambiarEtapa(id, request, actual);
    }

    @PostMapping("/{id}/miembros")
    @PreAuthorize("hasAnyRole('ADMIN', 'ARQUITECTO')")
    public ProyectoDetalleResponse agregarMiembro(@PathVariable Long id,
                                                  @Valid @RequestBody AgregarMiembroRequest request,
                                                  @AuthenticationPrincipal UsuarioAutenticado actual) {
        return proyectoService.agregarMiembro(id, request, actual);
    }

    @DeleteMapping("/{id}/miembros/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ARQUITECTO')")
    public ProyectoDetalleResponse quitarMiembro(@PathVariable Long id,
                                                 @PathVariable Long usuarioId,
                                                 @AuthenticationPrincipal UsuarioAutenticado actual) {
        return proyectoService.quitarMiembro(id, usuarioId, actual);
    }
}