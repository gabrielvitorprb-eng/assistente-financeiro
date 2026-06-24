package com.gabriel.assistentefinanceiro.model;

import com.gabriel.assistentefinanceiro.enums.IntencaoMensagem;
import com.gabriel.assistentefinanceiro.enums.StatusProcessamento;
import com.gabriel.assistentefinanceiro.enums.TipoMensagemWhatsapp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "mensagens_whatsapp")
public class MensagemWhatsapp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false, length = 1000)
    private String texto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMensagemWhatsapp tipoMensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntencaoMensagem intencao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcessamento status;

    @Column(nullable = false)
    private LocalDateTime recebidaEm;

    @Column(length = 1000)
    private String resposta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
