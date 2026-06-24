package com.gabriel.assistentefinanceiro.dto;

import java.math.BigDecimal;

public record ContaFixaResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        Integer diaVencimento,
        Boolean ativa,
        Long usuarioId
) {
}
