package com.gabriel.assistentefinanceiro.repository;

import com.gabriel.assistentefinanceiro.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByTelefoneWhatsapp(String telefoneWhatsapp);

    boolean existsByEmail(String email);

    boolean existsByTelefoneWhatsapp(String telefoneWhatsapp);
}
