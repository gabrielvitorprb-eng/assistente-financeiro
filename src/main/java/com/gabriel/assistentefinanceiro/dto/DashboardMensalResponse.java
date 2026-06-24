package com.gabriel.assistentefinanceiro.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardMensalResponse(
        Integer ano,
        Integer mes,
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldoMensal,
        List<GastoPorCategoriaResponse> gastosPorCategoria
) {
}
