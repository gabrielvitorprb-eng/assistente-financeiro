package com.gabriel.assistentefinanceiro.service;

import com.gabriel.assistentefinanceiro.dto.ContaFixaRequest;
import com.gabriel.assistentefinanceiro.dto.ContaFixaResponse;
import com.gabriel.assistentefinanceiro.model.ContaFixa;
import com.gabriel.assistentefinanceiro.repository.ContaFixaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContaFixaService {

    private final ContaFixaRepository contaFixaRepository;
    private final UsuarioService usuarioService;

    public ContaFixaResponse criar(ContaFixaRequest request) {
        ContaFixa conta = new ContaFixa();
        conta.setDescricao(request.descricao());
        conta.setValor(request.valor());
        conta.setDiaVencimento(request.diaVencimento());
        conta.setAtiva(request.ativa());
        conta.setUsuario(usuarioService.buscarPorId(request.usuarioId()));
        return toResponse(contaFixaRepository.save(conta));
    }

    public List<ContaFixaResponse> listar(Long usuarioId) {
        return contaFixaRepository.findByUsuarioId(usuarioId).stream().map(this::toResponse).toList();
    }

    private ContaFixaResponse toResponse(ContaFixa conta) {
        return new ContaFixaResponse(
                conta.getId(),
                conta.getDescricao(),
                conta.getValor(),
                conta.getDiaVencimento(),
                conta.getAtiva(),
                conta.getUsuario().getId()
        );
    }
}
