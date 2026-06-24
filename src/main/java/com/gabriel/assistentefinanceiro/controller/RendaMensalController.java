package com.gabriel.assistentefinanceiro.controller;

import com.gabriel.assistentefinanceiro.dto.RendaMensalRequest;
import com.gabriel.assistentefinanceiro.dto.RendaMensalResponse;
import com.gabriel.assistentefinanceiro.service.RendaMensalService;
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
@RequestMapping("/rendas-mensais")
@RequiredArgsConstructor
public class RendaMensalController {

    private final RendaMensalService rendaMensalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RendaMensalResponse salvar(@Valid @RequestBody RendaMensalRequest request) {
        return rendaMensalService.salvar(request);
    }

    @GetMapping
    public List<RendaMensalResponse> listar(@RequestParam Long usuarioId) {
        return rendaMensalService.listar(usuarioId);
    }
}
