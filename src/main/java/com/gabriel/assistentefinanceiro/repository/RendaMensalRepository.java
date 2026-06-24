package com.gabriel.assistentefinanceiro.repository;

import com.gabriel.assistentefinanceiro.model.RendaMensal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RendaMensalRepository extends JpaRepository<RendaMensal, Long> {
    List<RendaMensal> findByUsuarioId(Long usuarioId);

    Optional<RendaMensal> findByUsuarioIdAndAnoAndMes(Long usuarioId, Integer ano, Integer mes);
}
