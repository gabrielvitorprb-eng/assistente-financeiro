package com.gabriel.assistentefinanceiro.service;

import com.gabriel.assistentefinanceiro.dto.DashboardMensalResponse;
import com.gabriel.assistentefinanceiro.dto.GastoPorCategoriaResponse;
import com.gabriel.assistentefinanceiro.enums.TipoLancamento;
import com.gabriel.assistentefinanceiro.model.Lancamento;
import com.gabriel.assistentefinanceiro.repository.ContaFixaRepository;
import com.gabriel.assistentefinanceiro.repository.LancamentoRepository;
import com.gabriel.assistentefinanceiro.repository.RendaMensalRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LancamentoRepository lancamentoRepository;
    private final RendaMensalRepository rendaMensalRepository;
    private final ContaFixaRepository contaFixaRepository;

    @Transactional(readOnly = true)
    public DashboardMensalResponse mensal(Long usuarioId, Integer ano, Integer mes) {
        YearMonth periodo = YearMonth.of(ano, mes);
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.atEndOfMonth();

        List<Lancamento> lancamentos = lancamentoRepository.findByUsuarioIdAndDataBetweenComCategoria(usuarioId, inicio, fim);
        BigDecimal renda = rendaMensalRepository.findByUsuarioIdAndAnoAndMes(usuarioId, ano, mes)
                .map(rendaMensal -> rendaMensal.getValor())
                .orElse(BigDecimal.ZERO);
        BigDecimal entradas = somarLancamentos(lancamentos, TipoLancamento.ENTRADA).add(renda);
        BigDecimal contasFixas = contaFixaRepository.findByUsuarioIdAndAtivaTrue(usuarioId).stream()
                .map(conta -> conta.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saidasLancamentos = somarLancamentos(lancamentos, TipoLancamento.SAIDA);
        BigDecimal saidas = saidasLancamentos.add(contasFixas);

        Map<Long, List<Lancamento>> porCategoria = lancamentos.stream()
                .filter(lancamento -> lancamento.getTipo() == TipoLancamento.SAIDA)
                .collect(Collectors.groupingBy(lancamento -> lancamento.getCategoria().getId()));

        List<GastoPorCategoriaResponse> gastos = porCategoria.values().stream()
                .map(this::toGastoPorCategoria)
                .toList();

        return new DashboardMensalResponse(ano, mes, entradas, saidas, entradas.subtract(saidas), gastos);
    }

    private BigDecimal somarLancamentos(List<Lancamento> lancamentos, TipoLancamento tipo) {
        return lancamentos.stream()
                .filter(lancamento -> lancamento.getTipo() == tipo)
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private GastoPorCategoriaResponse toGastoPorCategoria(List<Lancamento> lancamentos) {
        Lancamento primeiro = lancamentos.getFirst();
        BigDecimal total = lancamentos.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new GastoPorCategoriaResponse(
                primeiro.getCategoria().getId(),
                primeiro.getCategoria().getNome(),
                total
        );
    }
}
