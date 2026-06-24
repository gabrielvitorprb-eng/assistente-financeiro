package com.gabriel.assistentefinanceiro.dto;

public record UsuarioResponse(
        Long usuarioId,
        String nome,
        String email,
        String telefoneWhatsapp
) {
}
