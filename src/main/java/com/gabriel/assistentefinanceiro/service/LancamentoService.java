package com.gabriel.assistentefinanceiro.service;

import com.gabriel.assistentefinanceiro.dto.LancamentoRequest;
import com.gabriel.assistentefinanceiro.dto.LancamentoResponse;
import com.gabriel.assistentefinanceiro.exception.RecursoNaoEncontradoException;
import com.gabriel.assistentefinanceiro.model.Categoria;
import com.gabriel.assistentefinanceiro.model.Lancamento;
import com.gabriel.assistentefinanceiro.model.Usuario;
import com.gabriel.assistentefinanceiro.repository.LancamentoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LancamentoService {

    private final LancamentoRepository lancamentoRepository;
    private final UsuarioService usuarioService;
    private final CategoriaService categoriaService;

    @Transactional
    public LancamentoResponse criar(LancamentoRequest request) {
        Lancamento lancamento = preencher(new Lancamento(), request);
        return toResponse(lancamentoRepository.save(lancamento));
    }

    @Transactional(readOnly = true)
    public List<LancamentoResponse> listar(Long usuarioId) {
        return lancamentoRepository.findByUsuarioIdComCategoria(usuarioId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LancamentoResponse buscar(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public LancamentoResponse atualizar(Long id, LancamentoRequest request) {
        Lancamento lancamento = preencher(buscarEntidade(id), request);
        return toResponse(lancamentoRepository.save(lancamento));
    }

    @Transactional
    public void excluir(Long id) {
        lancamentoRepository.delete(buscarEntidade(id));
    }

    private Lancamento preencher(Lancamento lancamento, LancamentoRequest request) {
        Usuario usuario = usuarioService.buscarPorId(request.usuarioId());
        Categoria categoria = categoriaService.buscarEntidade(request.categoriaId());
        if (!categoria.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Categoria nao pertence ao usuario informado");
        }
        lancamento.setDescricao(request.descricao());
        lancamento.setValor(request.valor());
        lancamento.setData(request.data());
        lancamento.setTipo(request.tipo());
        lancamento.setFormaPagamento(request.formaPagamento());
        lancamento.setUsuario(usuario);
        lancamento.setCategoria(categoria);
        return lancamento;
    }

    private Lancamento buscarEntidade(Long id) {
        return lancamentoRepository.findByIdComCategoria(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lancamento nao encontrado"));
    }

    private LancamentoResponse toResponse(Lancamento lancamento) {
        return new LancamentoResponse(
                lancamento.getId(),
                lancamento.getDescricao(),
                lancamento.getValor(),
                lancamento.getData(),
                lancamento.getTipo(),
                lancamento.getFormaPagamento(),
                lancamento.getUsuario().getId(),
                lancamento.getCategoria().getId(),
                lancamento.getCategoria().getNome()
        );
    }
}
