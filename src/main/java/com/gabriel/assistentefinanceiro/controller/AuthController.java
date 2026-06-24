package com.gabriel.assistentefinanceiro.controller;

import com.gabriel.assistentefinanceiro.dto.AuthRequest;
import com.gabriel.assistentefinanceiro.dto.UsuarioRegistroRequest;
import com.gabriel.assistentefinanceiro.dto.UsuarioResponse;
import com.gabriel.assistentefinanceiro.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody UsuarioRegistroRequest request) {
        return usuarioService.registrarUsuario(request);
    }

    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody AuthRequest request) {
        return usuarioService.login(request);
    }
}
