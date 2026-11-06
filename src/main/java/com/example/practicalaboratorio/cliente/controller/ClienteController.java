package com.example.practicalaboratorio.cliente.controller;

import com.example.practicalaboratorio.auth.security.UsuarioAutenticado;
import com.example.practicalaboratorio.cliente.dto.ClienteRequest;
import com.example.practicalaboratorio.cliente.dto.ClienteResponse;
import com.example.practicalaboratorio.cliente.entity.TipoCliente;
import com.example.practicalaboratorio.cliente.service.ClienteService;
import com.example.practicalaboratorio.common.dto.PaginaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public PaginaResponse<ClienteResponse> buscar(@RequestParam(required = false) String texto,
                                                  @RequestParam(required = false) TipoCliente tipo,
                                                  @RequestParam(defaultValue = "false") boolean incluirInactivos,
                                                  @RequestParam(defaultValue = "0") int pagina,
                                                  @RequestParam(defaultValue = "10") int tamanio,
                                                  @AuthenticationPrincipal UsuarioAutenticado actual) {
        return clienteService.buscar(texto, tipo, incluirInactivos, pagina, tamanio, actual);
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id,
                                   @AuthenticationPrincipal UsuarioAutenticado actual) {
        return clienteService.obtener(id, actual);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ARQUITECTO')")
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(@Valid @RequestBody ClienteRequest request,
                                 @AuthenticationPrincipal UsuarioAutenticado actual) {
        return clienteService.crear(request, actual);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ARQUITECTO')")
    public ClienteResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ClienteRequest request,
                                      @AuthenticationPrincipal UsuarioAutenticado actual) {
        return clienteService.actualizar(id, request, actual);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ARQUITECTO')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@PathVariable Long id,
                          @AuthenticationPrincipal UsuarioAutenticado actual) {
        clienteService.darDeBaja(id, actual);
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ARQUITECTO')")
    public ClienteResponse reactivar(@PathVariable Long id,
                                     @AuthenticationPrincipal UsuarioAutenticado actual) {
        return clienteService.reactivar(id, actual);
    }
}