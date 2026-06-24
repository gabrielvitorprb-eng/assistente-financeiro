package com.gabriel.assistentefinanceiro.service;

import com.gabriel.assistentefinanceiro.dto.AuthRequest;
import com.gabriel.assistentefinanceiro.dto.UsuarioPerfilRequest;
import com.gabriel.assistentefinanceiro.dto.UsuarioRegistroRequest;
import com.gabriel.assistentefinanceiro.dto.UsuarioResponse;
import com.gabriel.assistentefinanceiro.exception.RecursoNaoEncontradoException;
import com.gabriel.assistentefinanceiro.model.Categoria;
import com.gabriel.assistentefinanceiro.model.Usuario;
import com.gabriel.assistentefinanceiro.repository.CategoriaRepository;
import com.gabriel.assistentefinanceiro.repository.UsuarioRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse registrarUsuario(UsuarioRegistroRequest request) {
        String email = request.email().trim().toLowerCase();
        String telefoneWhatsapp = normalizarTelefone(request.telefoneWhatsapp());
        validarTelefoneNormalizado(telefoneWhatsapp);
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email ja cadastrado");
        }
        if (existeTelefoneWhatsappCadastrado(telefoneWhatsapp)) {
            throw new IllegalArgumentException("Telefone WhatsApp ja cadastrado");
        }
        Usuario usuario = usuarioRepository.save(new Usuario(
                request.nome().trim(),
                email,
                passwordEncoder.encode(request.senha()),
                telefoneWhatsapp
        ));
        criarCategoriasPadrao(usuario);
        return toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPerfil(Long id) {
        return toResponse(buscarPorId(id));
    }

    @Transactional
    public UsuarioResponse atualizarPerfil(Long id, UsuarioPerfilRequest request) {
        Usuario usuario = buscarPorId(id);
        String nome = request.nome().trim();
        String email = request.email().trim().toLowerCase();
        String telefoneWhatsapp = normalizarTelefone(request.telefoneWhatsapp());

        if (nome.isBlank()) {
            throw new IllegalArgumentException("Nome nao pode ficar vazio");
        }
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email nao pode ficar vazio");
        }
        validarTelefoneNormalizado(telefoneWhatsapp);

        usuarioRepository.findByEmail(email)
                .filter(outroUsuario -> !outroUsuario.getId().equals(id))
                .ifPresent(outroUsuario -> {
                    throw new IllegalArgumentException("Email ja cadastrado para outro usuario");
                });
        buscarUsuarioPorTelefonesPossiveis(telefoneWhatsapp)
                .filter(outroUsuario -> !outroUsuario.getId().equals(id))
                .ifPresent(outroUsuario -> {
                    throw new IllegalArgumentException("Telefone WhatsApp ja cadastrado para outro usuario");
                });

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefoneWhatsapp(telefoneWhatsapp);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse login(AuthRequest request) {
        String email = request.email().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email ou senha invalidos"));
        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new IllegalArgumentException("Email ou senha invalidos");
        }
        return toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorTelefoneWhatsapp(String telefoneWhatsapp) {
        String telefoneNormalizado = normalizarTelefone(telefoneWhatsapp);
        return buscarUsuarioPorTelefonesPossiveis(telefoneNormalizado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Telefone WhatsApp nao cadastrado"));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarOptionalPorTelefoneWhatsapp(String telefoneWhatsapp) {
        return buscarUsuarioPorTelefonesPossiveis(normalizarTelefone(telefoneWhatsapp));
    }

    public String normalizarTelefone(String telefone) {
        if (telefone == null) {
            return "";
        }
        String normalizado = telefone
                .replace("whatsapp:", "")
                .replace("+", "")
                .replace(" ", "")
                .replace("(", "")
                .replace(")", "")
                .replace("-", "")
                .replace(".", "")
                .trim();
        if (normalizado.matches("\\d{10,11}")) {
            return "55" + normalizado;
        }
        return normalizado;
    }

    private void validarTelefoneNormalizado(String telefoneWhatsapp) {
        if (!telefoneWhatsapp.matches("55\\d{10,11}")) {
            throw new IllegalArgumentException("Telefone WhatsApp deve estar no padrao 55 + DDD + numero com 8 ou 9 digitos");
        }
    }

    public List<String> telefonesWhatsappPossiveis(String telefoneWhatsapp) {
        String telefoneNormalizado = normalizarTelefone(telefoneWhatsapp);
        Set<String> telefones = new LinkedHashSet<>();
        telefones.add(telefoneNormalizado);
        if (telefoneNormalizado.matches("55\\d{10}")) {
            telefones.add(telefoneNormalizado.substring(0, 4) + "9" + telefoneNormalizado.substring(4));
        }
        if (telefoneNormalizado.matches("55\\d{11}") && telefoneNormalizado.charAt(4) == '9') {
            telefones.add(telefoneNormalizado.substring(0, 4) + telefoneNormalizado.substring(5));
        }
        return telefones.stream().toList();
    }

    private Optional<Usuario> buscarUsuarioPorTelefonesPossiveis(String telefoneWhatsapp) {
        return telefonesWhatsappPossiveis(telefoneWhatsapp).stream()
                .map(usuarioRepository::findByTelefoneWhatsapp)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private boolean existeTelefoneWhatsappCadastrado(String telefoneWhatsapp) {
        return buscarUsuarioPorTelefonesPossiveis(telefoneWhatsapp).isPresent();
    }

    @Transactional
    public void criarCategoriasPadrao(Usuario usuario) {
        categoriasPadrao().stream()
                .filter(categoria -> categoriaRepository
                        .findByUsuarioIdAndNomeIgnoreCase(usuario.getId(), categoria.nome())
                        .isEmpty())
                .map(categoria -> new Categoria(categoria.nome(), categoria.cor(), usuario))
                .forEach(categoriaRepository::save);
    }

    private List<CategoriaPadrao> categoriasPadrao() {
        return List.of(
                new CategoriaPadrao("Alimentacao", "#F97316"),
                new CategoriaPadrao("Moradia", "#2563EB"),
                new CategoriaPadrao("Transporte", "#16A34A"),
                new CategoriaPadrao("Lazer", "#9333EA"),
                new CategoriaPadrao("Saude", "#DC2626"),
                new CategoriaPadrao("WhatsApp", "#25D366"),
                new CategoriaPadrao("Pensao", "#DB2777"),
                new CategoriaPadrao("Salario", "#059669"),
                new CategoriaPadrao("Extra", "#0D9488"),
                new CategoriaPadrao("Bolsa", "#4F46E5"),
                new CategoriaPadrao("Outro", "#64748B"),
                new CategoriaPadrao("Educacao", "#7C3AED"),
                new CategoriaPadrao("Mercado", "#65A30D"),
                new CategoriaPadrao("Assinaturas", "#0891B2"),
                new CategoriaPadrao("Cartao", "#EA580C"),
                new CategoriaPadrao("Pix", "#14B8A6")
        );
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefoneWhatsapp()
        );
    }

    private record CategoriaPadrao(String nome, String cor) {
    }
}
