package com.gabriel.assistentefinanceiro.controller;

import com.gabriel.assistentefinanceiro.dto.UsuarioPerfilRequest;
import com.gabriel.assistentefinanceiro.dto.UsuarioResponse;
import com.gabriel.assistentefinanceiro.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/{id}")
    public UsuarioResponse buscarPerfil(@PathVariable Long id) {
        return usuarioService.buscarPerfil(id);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizarPerfil(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioPerfilRequest request
    ) {
        return usuarioService.atualizarPerfil(id, request);
    }
}
