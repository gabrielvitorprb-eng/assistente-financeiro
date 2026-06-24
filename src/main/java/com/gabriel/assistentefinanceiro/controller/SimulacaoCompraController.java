package com.gabriel.assistentefinanceiro.controller;

import com.gabriel.assistentefinanceiro.dto.SimulacaoCompraRequest;
import com.gabriel.assistentefinanceiro.dto.SimulacaoCompraResponse;
import com.gabriel.assistentefinanceiro.service.SimulacaoCompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulacoes")
@RequiredArgsConstructor
public class SimulacaoCompraController {

    private final SimulacaoCompraService simulacaoCompraService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SimulacaoCompraResponse simular(@Valid @RequestBody SimulacaoCompraRequest request) {
        return simulacaoCompraService.simular(request);
    }
}
