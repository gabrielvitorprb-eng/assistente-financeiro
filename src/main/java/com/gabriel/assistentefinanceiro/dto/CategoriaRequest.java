package com.gabriel.assistentefinanceiro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoriaRequest(
        @NotBlank String nome,
        @NotBlank String cor,
        @NotNull Long usuarioId
) {
}
