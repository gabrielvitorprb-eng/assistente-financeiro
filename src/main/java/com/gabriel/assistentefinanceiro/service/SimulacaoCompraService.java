package com.gabriel.assistentefinanceiro.service;

import com.gabriel.assistentefinanceiro.dto.DashboardMensalResponse;
import com.gabriel.assistentefinanceiro.dto.SimulacaoCompraRequest;
import com.gabriel.assistentefinanceiro.dto.SimulacaoCompraResponse;
import com.gabriel.assistentefinanceiro.model.SimulacaoCompra;
import com.gabriel.assistentefinanceiro.repository.SimulacaoCompraRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimulacaoCompraService {

    private final SimulacaoCompraRepository simulacaoCompraRepository;
    private final UsuarioService usuarioService;
    private final DashboardService dashboardService;

    public SimulacaoCompraResponse simular(SimulacaoCompraRequest request) {
        DashboardMensalResponse dashboard = dashboardService.mensal(
                request.usuarioId(),
                request.anoImpactado(),
                request.mesImpactado()
        );
        BigDecimal impacto = request.valor()
                .divide(BigDecimal.valueOf(request.quantidadeParcelas()), 2, RoundingMode.HALF_UP);
        BigDecimal saldoDepois = dashboard.saldoMensal().subtract(impacto);
        boolean suficiente = saldoDepois.compareTo(BigDecimal.ZERO) >= 0;

        SimulacaoCompra simulacao = new SimulacaoCompra();
        simulacao.setDescricao(request.descricao());
        simulacao.setValor(request.valor());
        simulacao.setFormaPagamento(request.formaPagamento());
        simulacao.setQuantidadeParcelas(request.quantidadeParcelas());
        simulacao.setAnoImpactado(request.anoImpactado());
        simulacao.setMesImpactado(request.mesImpactado());
        simulacao.setSaldoAntes(dashboard.saldoMensal());
        simulacao.setSaldoDepois(saldoDepois);
        simulacao.setSaldoSuficiente(suficiente);
        simulacao.setUsuario(usuarioService.buscarPorId(request.usuarioId()));

        return toResponse(simulacaoCompraRepository.save(simulacao));
    }

    private SimulacaoCompraResponse toResponse(SimulacaoCompra simulacao) {
        String mensagem = simulacao.getSaldoSuficiente()
                ? "Compra possivel dentro do saldo mensal."
                : "Compra pode comprometer o saldo mensal.";
        return new SimulacaoCompraResponse(
                simulacao.getId(),
                simulacao.getDescricao(),
                simulacao.getValor(),
                simulacao.getFormaPagamento(),
                simulacao.getQuantidadeParcelas(),
                simulacao.getAnoImpactado(),
                simulacao.getMesImpactado(),
                simulacao.getSaldoAntes(),
                simulacao.getSaldoDepois(),
                simulacao.getSaldoSuficiente(),
                mensagem
        );
    }
}
