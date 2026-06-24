package com.gabriel.assistentefinanceiro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ContaFixaRequest(
        @NotBlank String descricao,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotNull @Min(1) @Max(31) Integer diaVencimento,
        @NotNull Boolean ativa,
        @NotNull Long usuarioId
) {
}
