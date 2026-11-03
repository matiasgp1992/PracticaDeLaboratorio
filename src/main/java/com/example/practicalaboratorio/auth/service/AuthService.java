package com.example.practicalaboratorio.auth.service;

import com.example.practicalaboratorio.auth.dto.AuthResponse;
import com.example.practicalaboratorio.auth.dto.LoginRequest;
import com.example.practicalaboratorio.auth.dto.RegistroRequest;
import com.example.practicalaboratorio.auth.jwt.JwtService;
import com.example.practicalaboratorio.common.exception.ReglaNegocioException;
import com.example.practicalaboratorio.organizacion.entity.Organizacion;
import com.example.practicalaboratorio.organizacion.repository.OrganizacionRepository;
import com.example.practicalaboratorio.usuario.dto.UsuarioResponse;
import com.example.practicalaboratorio.usuario.entity.Rol;
import com.example.practicalaboratorio.usuario.entity.Usuario;
import com.example.practicalaboratorio.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OrganizacionRepository organizacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese email");
        }

        String cuit = (request.cuit() == null || request.cuit().isBlank()) ? null : request.cuit().trim();
        if (cuit != null && organizacionRepository.existsByCuit(cuit)) {
            throw new ReglaNegocioException("Ya existe un estudio registrado con ese CUIT");
        }

        Organizacion organizacion = new Organizacion();
        organizacion.setNombre(request.nombreOrganizacion().trim());
        organizacion.setCuit(cuit);
        organizacionRepository.save(organizacion);

        Usuario admin = new Usuario();
        admin.setNombre(request.nombre().trim());
        admin.setApellido(request.apellido().trim());
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(request.password()));
        admin.setRol(Rol.ADMIN);
        admin.setOrganizacion(organizacion);
        usuarioRepository.save(admin);

        return new AuthResponse(jwtService.generarToken(admin), UsuarioResponse.desde(admin));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        return new AuthResponse(jwtService.generarToken(usuario), UsuarioResponse.desde(usuario));
    }
}