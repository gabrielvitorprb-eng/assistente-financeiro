package com.gabriel.assistentefinanceiro.service;

import com.gabriel.assistentefinanceiro.dto.DashboardMensalResponse;
import com.gabriel.assistentefinanceiro.dto.LancamentoRequest;
import com.gabriel.assistentefinanceiro.dto.WhatsAppMensagemRequest;
import com.gabriel.assistentefinanceiro.dto.WhatsAppMensagemResponse;
import com.gabriel.assistentefinanceiro.enums.FormaPagamento;
import com.gabriel.assistentefinanceiro.enums.IntencaoMensagem;
import com.gabriel.assistentefinanceiro.enums.StatusProcessamento;
import com.gabriel.assistentefinanceiro.enums.TipoLancamento;
import com.gabriel.assistentefinanceiro.enums.TipoMensagemWhatsapp;
import com.gabriel.assistentefinanceiro.model.Categoria;
import com.gabriel.assistentefinanceiro.model.MensagemWhatsapp;
import com.gabriel.assistentefinanceiro.model.Usuario;
import com.gabriel.assistentefinanceiro.repository.MensagemWhatsappRepository;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WhatsappService {

    private static final Locale LOCALE_BR = Locale.of("pt", "BR");
    private static final Pattern VALOR_PATTERN = Pattern.compile("\\d{1,3}(?:\\.\\d{3})+(?:,\\d{1,2})?|\\d+(?:[.,]\\d{1,2})?");

    private final UsuarioService usuarioService;
    private final CategoriaService categoriaService;
    private final LancamentoService lancamentoService;
    private final DashboardService dashboardService;
    private final MensagemWhatsappRepository mensagemWhatsappRepository;

    @Transactional
    public WhatsAppMensagemResponse processar(WhatsAppMensagemRequest request) {
        String telefoneNormalizado = usuarioService.normalizarTelefone(request.telefone());
        Optional<Usuario> usuarioEncontrado = buscarUsuarioWhatsapp(telefoneNormalizado);
        IntencaoMensagem intencao = identificarIntencao(request.mensagem());
        ProcessamentoResult resultado;

        if (usuarioEncontrado.isEmpty()) {
            resultado = new ProcessamentoResult(
                    "Este telefone WhatsApp nao esta cadastrado. Cadastre seu usuario antes de usar o assistente.",
                    StatusProcessamento.IGNORADA
            );
            return new WhatsAppMensagemResponse(telefoneNormalizado, resultado.resposta(), intencao, resultado.status());
        } else {
            Usuario usuario = usuarioEncontrado.get();
            try {
                resultado = switch (intencao) {
                    case CADASTRAR_GASTO -> processado(cadastrarLancamentoWhatsapp(usuario, request.mensagem(), TipoLancamento.SAIDA));
                    case CADASTRAR_ENTRADA -> processado(cadastrarLancamentoWhatsapp(usuario, request.mensagem(), TipoLancamento.ENTRADA));
                    case CONSULTAR_SALDO -> processado(responderSaldo(usuario));
                    case CONSULTAR_GASTOS -> processado(responderGastos(usuario));
                    case SIMULAR_COMPRA -> processado("Envie a simulacao pela API /simulacoes com valor, parcelas e mes impactado.");
                    case DESCONHECIDA -> new ProcessamentoResult(
                            "Nao entendi sua mensagem. Por enquanto aceito texto para gasto, entrada, saldo e gastos do mes.",
                            StatusProcessamento.IGNORADA
                    );
                };
            } catch (RuntimeException exception) {
                resultado = new ProcessamentoResult(
                        "Nao consegui processar a mensagem: " + exception.getMessage(),
                        StatusProcessamento.ERRO
                );
            }
        }

        MensagemWhatsapp mensagem = new MensagemWhatsapp();
        mensagem.setTelefone(telefoneNormalizado);
        mensagem.setTexto(request.mensagem());
        mensagem.setTipoMensagem(TipoMensagemWhatsapp.TEXTO);
        mensagem.setIntencao(intencao);
        mensagem.setStatus(resultado.status());
        mensagem.setRecebidaEm(LocalDateTime.now());
        mensagem.setResposta(resultado.resposta());
        usuarioEncontrado.ifPresent(mensagem::setUsuario);
        mensagemWhatsappRepository.save(mensagem);

        return new WhatsAppMensagemResponse(telefoneNormalizado, resultado.resposta(), intencao, resultado.status());
    }

    private Optional<Usuario> buscarUsuarioWhatsapp(String telefoneNormalizado) {
        return usuarioService.buscarOptionalPorTelefoneWhatsapp(telefoneNormalizado);
    }

    private String cadastrarLancamentoWhatsapp(Usuario usuario, String texto, TipoLancamento tipo) {
        ValorExtraido valorExtraido = extrairValor(texto)
                .orElseThrow(() -> new IllegalArgumentException("informe um valor na mensagem"));
        String descricao = montarDescricao(texto, valorExtraido);
        Categoria categoria = detectarCategoria(usuario, texto, descricao);
        lancamentoService.criar(new LancamentoRequest(
                descricao,
                valorExtraido.valor(),
                LocalDate.now(),
                tipo,
                detectarFormaPagamento(texto),
                usuario.getId(),
                categoria.getId()
        ));
        String nomeTipo = tipo == TipoLancamento.SAIDA ? "gasto" : "entrada";
        return "Ok, registrei o " + nomeTipo + " de " + formatarMoeda(valorExtraido.valor())
                + " como \"" + descricao + "\".";
    }

    private String responderSaldo(Usuario usuario) {
        LocalDate hoje = LocalDate.now();
        DashboardMensalResponse dashboard = dashboardService.mensal(usuario.getId(), hoje.getYear(), hoje.getMonthValue());
        return "Neste mes voce teve " + formatarMoeda(dashboard.totalEntradas())
                + " de entradas, " + formatarMoeda(dashboard.totalSaidas())
                + " de saidas e saldo de " + formatarMoeda(dashboard.saldoMensal()) + ".";
    }

    private String responderGastos(Usuario usuario) {
        LocalDate hoje = LocalDate.now();
        DashboardMensalResponse dashboard = dashboardService.mensal(usuario.getId(), hoje.getYear(), hoje.getMonthValue());
        return "Voce gastou " + formatarMoeda(dashboard.totalSaidas()) + " neste mes.";
    }

    private IntencaoMensagem identificarIntencao(String mensagem) {
        String texto = normalizar(mensagem);
        if (texto.contains("saldo")) {
            return IntencaoMensagem.CONSULTAR_SALDO;
        }
        if (texto.contains("quanto gastei") || texto.contains("gastos do mes") || texto.equals("gastos") || texto.equals("gastos mes")) {
            return IntencaoMensagem.CONSULTAR_GASTOS;
        }
        if (texto.contains("simular") || texto.contains("posso comprar")) {
            return IntencaoMensagem.SIMULAR_COMPRA;
        }
        if (texto.matches(".*\\b(entrada|recebi|ganhei|salario|deposito|depositaram)\\b.*")) {
            return IntencaoMensagem.CADASTRAR_ENTRADA;
        }
        if (texto.matches(".*\\b(gasto|gastei|paguei|comprei|saida|saidas)\\b.*")) {
            return IntencaoMensagem.CADASTRAR_GASTO;
        }
        return IntencaoMensagem.DESCONHECIDA;
    }

    private Optional<ValorExtraido> extrairValor(String texto) {
        Matcher matcher = VALOR_PATTERN.matcher(texto);
        if (!matcher.find()) {
            return Optional.empty();
        }
        String textoValor = matcher.group();
        return Optional.of(new ValorExtraido(parseValor(textoValor), matcher.start(), matcher.end()));
    }

    private BigDecimal parseValor(String textoValor) {
        String normalizado = textoValor;
        if (normalizado.contains(".") && normalizado.contains(",")) {
            normalizado = normalizado.replace(".", "").replace(",", ".");
        } else if (normalizado.contains(",")) {
            normalizado = normalizado.replace(",", ".");
        } else if (normalizado.matches("\\d{1,3}(\\.\\d{3})+")) {
            normalizado = normalizado.replace(".", "");
        }
        return new BigDecimal(normalizado);
    }

    private String montarDescricao(String texto, ValorExtraido valorExtraido) {
        String semValor = texto.substring(0, valorExtraido.inicio()) + " " + texto.substring(valorExtraido.fim());
        String descricao = normalizar(semValor)
                .replaceAll("\\b(gasto|gastei|paguei|comprei|saida|saidas|entrada|recebi|ganhei|deposito|depositaram)\\b", " ")
                .replaceAll("\\b(reais|real|rs|r\\$|com|no|na|num|numa|em|de|do|da|o|a)\\b", " ")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (descricao.isBlank()) {
            return "Lancamento via WhatsApp";
        }
        return descricao;
    }

    private Categoria detectarCategoria(Usuario usuario, String textoOriginal, String descricao) {
        String texto = normalizar(textoOriginal + " " + descricao);
        Optional<String> nomeCategoria = Optional.empty();

        if (contemPalavra(texto, "salario")) {
            nomeCategoria = Optional.of("Salario");
        } else if (contemPalavra(texto, "extra")) {
            nomeCategoria = Optional.of("Extra");
        } else if (contemPalavra(texto, "bolsa")) {
            nomeCategoria = Optional.of("Bolsa");
        } else if (contemPalavra(texto, "pensao")) {
            nomeCategoria = Optional.of("Pensao");
        } else if (contemPalavra(texto, "mercado")) {
            nomeCategoria = Optional.of("Mercado");
        } else if (contemPalavra(texto, "faculdade", "curso", "livro", "educacao")) {
            nomeCategoria = Optional.of("Educacao");
        } else if (contemPalavra(texto, "cartao")) {
            nomeCategoria = Optional.of("Cartao");
        } else if (contemPalavra(texto, "pix")) {
            nomeCategoria = Optional.of("Pix");
        } else if (contemPalavra(texto, "assinatura", "netflix", "spotify", "prime")) {
            nomeCategoria = Optional.of("Assinaturas");
        } else if (contemPalavra(texto, "transporte", "uber", "onibus", "gasolina")) {
            nomeCategoria = Optional.of("Transporte");
        } else if (contemPalavra(texto, "lanche", "comida", "restaurante", "ifood")) {
            nomeCategoria = Optional.of("Alimentacao");
        }

        return nomeCategoria
                .map(nome -> categoriaService.buscarOuCriarPorNome(usuario, nome))
                .or(() -> categoriaService.buscarPorTexto(usuario, descricao))
                .orElseGet(() -> categoriaService.buscarOutroOuPadrao(usuario));
    }

    private boolean contemPalavra(String texto, String... palavras) {
        for (String palavra : palavras) {
            if (texto.matches(".*\\b" + Pattern.quote(palavra) + "\\b.*")) {
                return true;
            }
        }
        return false;
    }

    private FormaPagamento detectarFormaPagamento(String texto) {
        String normalizado = normalizar(texto);
        if (normalizado.contains("pix")) {
            return FormaPagamento.PIX;
        }
        if (normalizado.contains("debito")) {
            return FormaPagamento.DEBITO;
        }
        if (normalizado.contains("credito") || normalizado.contains("cartao")) {
            return FormaPagamento.CREDITO;
        }
        if (normalizado.contains("boleto")) {
            return FormaPagamento.BOLETO;
        }
        if (normalizado.contains("transferencia")) {
            return FormaPagamento.TRANSFERENCIA;
        }
        if (normalizado.contains("dinheiro")) {
            return FormaPagamento.DINHEIRO;
        }
        return FormaPagamento.OUTRO;
    }

    private String normalizar(String texto) {
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT);
    }

    private String formatarMoeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(LOCALE_BR).format(valor);
    }

    private ProcessamentoResult processado(String resposta) {
        return new ProcessamentoResult(resposta, StatusProcessamento.PROCESSADA);
    }

    private record ProcessamentoResult(String resposta, StatusProcessamento status) {
    }

    private record ValorExtraido(BigDecimal valor, int inicio, int fim) {
    }
}
