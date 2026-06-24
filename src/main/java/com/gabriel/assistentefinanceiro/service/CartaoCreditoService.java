package com.gabriel.assistentefinanceiro.service;

import com.gabriel.assistentefinanceiro.dto.CartaoCreditoRequest;
import com.gabriel.assistentefinanceiro.dto.CartaoCreditoResponse;
import com.gabriel.assistentefinanceiro.model.CartaoCredito;
import com.gabriel.assistentefinanceiro.repository.CartaoCreditoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartaoCreditoService {

    private final CartaoCreditoRepository cartaoCreditoRepository;
    private final UsuarioService usuarioService;

    public CartaoCreditoResponse criar(CartaoCreditoRequest request) {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setNome(request.nome());
        cartao.setLimite(request.limite());
        cartao.setDiaFechamento(request.diaFechamento());
        cartao.setDiaVencimento(request.diaVencimento());
        cartao.setUsuario(usuarioService.buscarPorId(request.usuarioId()));
        return toResponse(cartaoCreditoRepository.save(cartao));
    }

    public List<CartaoCreditoResponse> listar(Long usuarioId) {
        return cartaoCreditoRepository.findByUsuarioId(usuarioId).stream().map(this::toResponse).toList();
    }

    private CartaoCreditoResponse toResponse(CartaoCredito cartao) {
        return new CartaoCreditoResponse(
                cartao.getId(),
                cartao.getNome(),
                cartao.getLimite(),
                cartao.getDiaFechamento(),
                cartao.getDiaVencimento(),
                cartao.getUsuario().getId()
        );
    }
}
