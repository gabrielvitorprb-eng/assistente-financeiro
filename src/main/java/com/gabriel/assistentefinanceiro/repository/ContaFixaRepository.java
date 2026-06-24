package com.gabriel.assistentefinanceiro.repository;

import com.gabriel.assistentefinanceiro.model.ContaFixa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaFixaRepository extends JpaRepository<ContaFixa, Long> {
    List<ContaFixa> findByUsuarioId(Long usuarioId);

    List<ContaFixa> findByUsuarioIdAndAtivaTrue(Long usuarioId);
}
