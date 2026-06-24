package com.gabriel.assistentefinanceiro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioPerfilRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank String telefoneWhatsapp
) {
}
