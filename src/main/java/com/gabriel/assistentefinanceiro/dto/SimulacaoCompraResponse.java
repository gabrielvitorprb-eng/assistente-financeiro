package com.gabriel.assistentefinanceiro.dto;

import com.gabriel.assistentefinanceiro.enums.FormaPagamento;
import java.math.BigDecimal;

public record SimulacaoCompraResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        FormaPagamento formaPagamento,
        Integer quantidadeParcelas,
        Integer anoImpactado,
        Integer mesImpactado,
        BigDecimal saldoAntes,
        BigDecimal saldoDepois,
        Boolean saldoSuficiente,
        String mensagem
) {
}
