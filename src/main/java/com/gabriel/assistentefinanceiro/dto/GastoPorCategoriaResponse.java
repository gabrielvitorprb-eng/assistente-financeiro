package com.gabriel.assistentefinanceiro.dto;

import java.math.BigDecimal;

public record GastoPorCategoriaResponse(
        Long categoriaId,
        String categoriaNome,
        BigDecimal total
) {
}
