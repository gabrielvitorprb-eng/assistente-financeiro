package com.gabriel.assistentefinanceiro.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false, unique = true)
    private String telefoneWhatsapp;

    @Column(name = "telefone")
    private String telefoneLegado;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    public Usuario(String nome, String email, String senha, String telefoneWhatsapp) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefoneWhatsapp = telefoneWhatsapp;
        this.telefoneLegado = telefoneWhatsapp;
    }

    @PrePersist
    void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        sincronizarTelefoneLegado();
    }

    @PreUpdate
    void preUpdate() {
        sincronizarTelefoneLegado();
    }

    private void sincronizarTelefoneLegado() {
        telefoneLegado = telefoneWhatsapp;
    }
}
