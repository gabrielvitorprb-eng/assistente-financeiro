package com.gabriel.assistentefinanceiro.controller;

import com.gabriel.assistentefinanceiro.dto.ContaFixaRequest;
import com.gabriel.assistentefinanceiro.dto.ContaFixaResponse;
import com.gabriel.assistentefinanceiro.service.ContaFixaService;
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
@RequestMapping("/contas-fixas")
@RequiredArgsConstructor
public class ContaFixaController {

    private final ContaFixaService contaFixaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContaFixaResponse criar(@Valid @RequestBody ContaFixaRequest request) {
        return contaFixaService.criar(request);
    }

    @GetMapping
    public List<ContaFixaResponse> listar(@RequestParam Long usuarioId) {
        return contaFixaService.listar(usuarioId);
    }
}
