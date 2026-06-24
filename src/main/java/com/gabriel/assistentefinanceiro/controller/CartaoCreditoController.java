package com.gabriel.assistentefinanceiro.controller;

import com.gabriel.assistentefinanceiro.dto.CartaoCreditoRequest;
import com.gabriel.assistentefinanceiro.dto.CartaoCreditoResponse;
import com.gabriel.assistentefinanceiro.service.CartaoCreditoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cartoes-credito")
@RequiredArgsConstructor
public class CartaoCreditoController {

    private final CartaoCreditoService cartaoCreditoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartaoCreditoResponse criar(@Valid @RequestBody CartaoCreditoRequest request) {
        return cartaoCreditoService.criar(request);
    }

    @GetMapping
    public List<CartaoCreditoResponse> listar(@RequestParam Long usuarioId) {
        return cartaoCreditoService.listar(usuarioId);
    }
}
