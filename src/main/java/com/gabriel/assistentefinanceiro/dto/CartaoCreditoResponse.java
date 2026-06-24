package com.gabriel.assistentefinanceiro.dto;

import java.math.BigDecimal;

public record CartaoCreditoResponse(
        Long id,
        String nome,
        BigDecimal limite,
        Integer diaFechamento,
        Integer diaVencimento,
        Long usuarioId
) {
}
