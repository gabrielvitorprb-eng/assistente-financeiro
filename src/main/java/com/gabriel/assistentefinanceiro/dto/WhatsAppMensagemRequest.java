package com.gabriel.assistentefinanceiro.dto;

import jakarta.validation.constraints.NotBlank;

public record WhatsAppMensagemRequest(
        @NotBlank String telefone,
        @NotBlank String mensagem
) {
}
