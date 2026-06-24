package com.gabriel.assistentefinanceiro.model;

import com.gabriel.assistentefinanceiro.enums.FormaPagamento;
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
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "simulacoes_compra")
public class SimulacaoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamento formaPagamento;

    @Column(nullable = false)
    private Integer quantidadeParcelas;

    @Column(nullable = false)
    private Integer anoImpactado;

    @Column(nullable = false)
    private Integer mesImpactado;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoAntes;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoDepois;

    @Column(nullable = false)
    private Boolean saldoSuficiente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
