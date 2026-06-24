package com.gabriel.assistentefinanceiro.repository;

import com.gabriel.assistentefinanceiro.model.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByUsuarioId(Long usuarioId);

    Optional<Categoria> findByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);
}
