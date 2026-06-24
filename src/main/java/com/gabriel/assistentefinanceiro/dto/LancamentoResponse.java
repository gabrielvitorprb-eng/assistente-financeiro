package com.gabriel.assistentefinanceiro.dto;

import com.gabriel.assistentefinanceiro.enums.FormaPagamento;
import com.gabriel.assistentefinanceiro.enums.TipoLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        TipoLancamento tipo,
        FormaPagamento formaPagamento,
        Long usuarioId,
        Long categoriaId,
        String categoriaNome
) {
}
