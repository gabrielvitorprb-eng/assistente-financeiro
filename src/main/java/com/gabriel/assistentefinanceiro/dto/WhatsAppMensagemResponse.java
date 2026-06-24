package com.gabriel.assistentefinanceiro.dto;

import com.gabriel.assistentefinanceiro.enums.IntencaoMensagem;
import com.gabriel.assistentefinanceiro.enums.StatusProcessamento;

public record WhatsAppMensagemResponse(
        String telefone,
        String resposta,
        IntencaoMensagem intencao,
        StatusProcessamento status
) {
}
