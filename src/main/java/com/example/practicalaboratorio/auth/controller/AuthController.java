package com.example.practicalaboratorio.auth.controller;

import com.example.practicalaboratorio.auth.dto.AuthResponse;
import com.example.practicalaboratorio.auth.dto.LoginRequest;
import com.example.practicalaboratorio.auth.dto.RegistroRequest;
import com.example.practicalaboratorio.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}