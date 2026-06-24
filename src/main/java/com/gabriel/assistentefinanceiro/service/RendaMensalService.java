package com.gabriel.assistentefinanceiro.service;

import com.gabriel.assistentefinanceiro.dto.RendaMensalRequest;
import com.gabriel.assistentefinanceiro.dto.RendaMensalResponse;
import com.gabriel.assistentefinanceiro.model.RendaMensal;
import com.gabriel.assistentefinanceiro.repository.RendaMensalRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RendaMensalService {

    private final RendaMensalRepository rendaMensalRepository;
    private final UsuarioService usuarioService;

    public RendaMensalResponse salvar(RendaMensalRequest request) {
        RendaMensal renda = rendaMensalRepository
                .findByUsuarioIdAndAnoAndMes(request.usuarioId(), request.ano(), request.mes())
                .orElseGet(RendaMensal::new);
        renda.setAno(request.ano());
        renda.setMes(request.mes());
        renda.setValor(request.valor());
        renda.setUsuario(usuarioService.buscarPorId(request.usuarioId()));
        return toResponse(rendaMensalRepository.save(renda));
    }

    public List<RendaMensalResponse> listar(Long usuarioId) {
        return rendaMensalRepository.findByUsuarioId(usuarioId).stream().map(this::toResponse).toList();
    }

    private RendaMensalResponse toResponse(RendaMensal renda) {
        return new RendaMensalResponse(
                renda.getId(),
                renda.getAno(),
                renda.getMes(),
                renda.getValor(),
                renda.getUsuario().getId()
        );
    }
}
