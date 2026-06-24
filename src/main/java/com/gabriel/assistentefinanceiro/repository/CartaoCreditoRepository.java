package com.gabriel.assistentefinanceiro.repository;

import com.gabriel.assistentefinanceiro.model.CartaoCredito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartaoCreditoRepository extends JpaRepository<CartaoCredito, Long> {
    List<CartaoCredito> findByUsuarioId(Long usuarioId);
}
