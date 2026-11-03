package com.example.practicalaboratorio.usuario.service;

import com.example.practicalaboratorio.auth.security.UsuarioAutenticado;
import com.example.practicalaboratorio.common.exception.RecursoNoEncontradoException;
import com.example.practicalaboratorio.common.exception.ReglaNegocioException;
import com.example.practicalaboratorio.organizacion.entity.Organizacion;
import com.example.practicalaboratorio.organizacion.repository.OrganizacionRepository;
import com.example.practicalaboratorio.usuario.dto.CrearUsuarioRequest;
import com.example.practicalaboratorio.usuario.dto.UsuarioResponse;
import com.example.practicalaboratorio.usuario.entity.Usuario;
import com.example.practicalaboratorio.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final OrganizacionRepository organizacionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerActual(UsuarioAutenticado actual) {
        return UsuarioResponse.desde(buscarEnMiOrganizacion(actual.id(), actual));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(UsuarioAutenticado actual) {
        return usuarioRepository.findByOrganizacionId(actual.organizacionId())
                .stream()
                .map(UsuarioResponse::desde)
                .toList();
    }

    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest request, UsuarioAutenticado actual) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese email");
        }

        Organizacion organizacion = organizacionRepository.getReferenceById(actual.organizacionId());

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        usuario.setOrganizacion(organizacion);
        usuarioRepository.save(usuario);

        return UsuarioResponse.desde(usuario);
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo, UsuarioAutenticado actual) {
        if (id.equals(actual.id()) && !activo) {
            throw new ReglaNegocioException("No podés desactivar tu propio usuario");
        }
        Usuario usuario = buscarEnMiOrganizacion(id, actual);
        usuario.setActivo(activo);
        return UsuarioResponse.desde(usuario);
    }

    private Usuario buscarEnMiOrganizacion(Long id, UsuarioAutenticado actual) {
        return usuarioRepository.findByIdAndOrganizacionId(id, actual.organizacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}