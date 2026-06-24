package com.gabriel.assistentefinanceiro.service;

import com.gabriel.assistentefinanceiro.dto.CategoriaRequest;
import com.gabriel.assistentefinanceiro.dto.CategoriaResponse;
import com.gabriel.assistentefinanceiro.exception.RecursoNaoEncontradoException;
import com.gabriel.assistentefinanceiro.model.Categoria;
import com.gabriel.assistentefinanceiro.model.Usuario;
import com.gabriel.assistentefinanceiro.repository.CategoriaRepository;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        Usuario usuario = usuarioService.buscarPorId(request.usuarioId());
        Categoria categoria = new Categoria(request.nome(), request.cor(), usuario);
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar(Long usuarioId) {
        return categoriaRepository.findByUsuarioId(usuarioId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarEntidade(id);
        categoria.setNome(request.nome());
        categoria.setCor(request.cor());
        categoria.setUsuario(usuarioService.buscarPorId(request.usuarioId()));
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void excluir(Long id) {
        categoriaRepository.delete(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria nao encontrada"));
    }

    @Transactional
    public Categoria buscarOuCriarPadrao(Usuario usuario) {
        return categoriaRepository.findByUsuarioIdAndNomeIgnoreCase(usuario.getId(), "WhatsApp")
                .orElseGet(() -> categoriaRepository.save(new Categoria("WhatsApp", "#25D366", usuario)));
    }

    @Transactional
    public Categoria buscarOuCriarPorNome(Usuario usuario, String nome) {
        return categoriaRepository.findByUsuarioIdAndNomeIgnoreCase(usuario.getId(), nome)
                .orElseGet(() -> categoriaRepository.save(new Categoria(nome, corPadrao(nome), usuario)));
    }

    @Transactional
    public Categoria buscarOutroOuPadrao(Usuario usuario) {
        return categoriaRepository.findByUsuarioIdAndNomeIgnoreCase(usuario.getId(), "Outro")
                .orElseGet(() -> buscarOuCriarPadrao(usuario));
    }

    @Transactional
    public Categoria buscarPorTextoOuPadrao(Usuario usuario, String texto) {
        return buscarPorTexto(usuario, texto)
                .orElseGet(() -> buscarOuCriarPadrao(usuario));
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorTexto(Usuario usuario, String texto) {
        String textoNormalizado = normalizar(texto);
        return categoriaRepository.findByUsuarioId(usuario.getId()).stream()
                .filter(categoria -> textoNormalizado.contains(normalizar(categoria.getNome())))
                .findFirst();
    }

    private String corPadrao(String nome) {
        return switch (normalizar(nome)) {
            case "alimentacao" -> "#F97316";
            case "moradia" -> "#2563EB";
            case "transporte" -> "#16A34A";
            case "lazer" -> "#9333EA";
            case "saude" -> "#DC2626";
            case "whatsapp" -> "#25D366";
            case "pensao" -> "#DB2777";
            case "salario" -> "#059669";
            case "extra" -> "#0D9488";
            case "bolsa" -> "#4F46E5";
            case "outro" -> "#64748B";
            case "educacao" -> "#7C3AED";
            case "mercado" -> "#65A30D";
            case "assinaturas" -> "#0891B2";
            case "cartao" -> "#EA580C";
            case "pix" -> "#14B8A6";
            default -> "#64748B";
        };
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getCor(),
                categoria.getUsuario().getId()
        );
    }

    private String normalizar(String texto) {
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT);
    }
}
