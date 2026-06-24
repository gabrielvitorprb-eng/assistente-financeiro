package com.gabriel.assistentefinanceiro.dto;

import com.gabriel.assistentefinanceiro.enums.FormaPagamento;
import com.gabriel.assistentefinanceiro.enums.TipoLancamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoRequest(
        @NotBlank String descricao,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotNull LocalDate data,
        @NotNull TipoLancamento tipo,
        @NotNull FormaPagamento formaPagamento,
        @NotNull Long usuarioId,
        @NotNull Long categoriaId
) {
}
