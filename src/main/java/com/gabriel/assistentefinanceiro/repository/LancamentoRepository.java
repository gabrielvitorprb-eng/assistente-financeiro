package com.gabriel.assistentefinanceiro.repository;

import com.gabriel.assistentefinanceiro.enums.TipoLancamento;
import com.gabriel.assistentefinanceiro.model.Lancamento;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    @Query("""
            select l from Lancamento l
            join fetch l.usuario
            join fetch l.categoria
            where l.usuario.id = :usuarioId
            """)
    List<Lancamento> findByUsuarioIdComCategoria(@Param("usuarioId") Long usuarioId);

    @Query("""
            select l from Lancamento l
            join fetch l.usuario
            join fetch l.categoria
            where l.usuario.id = :usuarioId
            and l.data between :inicio and :fim
            """)
    List<Lancamento> findByUsuarioIdAndDataBetweenComCategoria(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("""
            select l from Lancamento l
            join fetch l.usuario
            join fetch l.categoria
            where l.id = :id
            """)
    Optional<Lancamento> findByIdComCategoria(@Param("id") Long id);

    List<Lancamento> findByUsuarioIdAndTipoAndDataBetween(Long usuarioId, TipoLancamento tipo, LocalDate inicio, LocalDate fim);
}
