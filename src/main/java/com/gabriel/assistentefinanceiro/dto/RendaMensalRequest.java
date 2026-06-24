package com.gabriel.assistentefinanceiro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RendaMensalRequest(
        @NotNull @Min(2000) Integer ano,
        @NotNull @Min(1) @Max(12) Integer mes,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotNull Long usuarioId
) {
}
