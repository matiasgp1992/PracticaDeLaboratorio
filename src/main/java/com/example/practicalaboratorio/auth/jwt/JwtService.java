package com.example.practicalaboratorio.auth.jwt;

import com.example.practicalaboratorio.auth.security.UsuarioAutenticado;
import com.example.practicalaboratorio.usuario.entity.Rol;
import com.example.practicalaboratorio.usuario.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId())
                .claim("org", usuario.getOrganizacion().getId())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    public UsuarioAutenticado leerToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new UsuarioAutenticado(
                ((Number) claims.get("uid")).longValue(),
                claims.getSubject(),
                ((Number) claims.get("org")).longValue(),
                Rol.valueOf(claims.get("rol", String.class))
        );
    }
}