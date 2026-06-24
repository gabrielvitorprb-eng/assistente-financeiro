package com.gabriel.assistentefinanceiro.repository;

import com.gabriel.assistentefinanceiro.model.MensagemWhatsapp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensagemWhatsappRepository extends JpaRepository<MensagemWhatsapp, Long> {
}
