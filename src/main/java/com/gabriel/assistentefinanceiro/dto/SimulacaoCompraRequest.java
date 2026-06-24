package com.gabriel.assistentefinanceiro.dto;

import com.gabriel.assistentefinanceiro.enums.FormaPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SimulacaoCompraRequest(
        @NotBlank String descricao,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotNull FormaPagamento formaPagamento,
        @NotNull @Min(1) Integer quantidadeParcelas,
        @NotNull @Min(2000) Integer anoImpactado,
        @NotNull @Min(1) @Max(12) Integer mesImpactado,
        @NotNull Long usuarioId
) {
}
