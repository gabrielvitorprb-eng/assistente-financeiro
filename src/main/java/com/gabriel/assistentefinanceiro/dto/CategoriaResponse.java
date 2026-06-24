package com.gabriel.assistentefinanceiro.dto;

public record CategoriaResponse(
        Long id,
        String nome,
        String cor,
        Long usuarioId
) {
}
