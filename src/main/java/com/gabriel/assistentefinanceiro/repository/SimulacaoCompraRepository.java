package com.gabriel.assistentefinanceiro.repository;

import com.gabriel.assistentefinanceiro.model.SimulacaoCompra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulacaoCompraRepository extends JpaRepository<SimulacaoCompra, Long> {
    List<SimulacaoCompra> findByUsuarioId(Long usuarioId);
}
