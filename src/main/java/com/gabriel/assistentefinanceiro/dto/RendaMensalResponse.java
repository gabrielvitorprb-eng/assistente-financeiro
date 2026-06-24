package com.gabriel.assistentefinanceiro.dto;

import java.math.BigDecimal;

public record RendaMensalResponse(
        Long id,
        Integer ano,
        Integer mes,
        BigDecimal valor,
        Long usuarioId
) {
}
