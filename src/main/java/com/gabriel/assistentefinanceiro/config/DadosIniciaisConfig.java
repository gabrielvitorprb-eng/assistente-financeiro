package com.gabriel.assistentefinanceiro.config;

import com.gabriel.assistentefinanceiro.repository.UsuarioRepository;
import com.gabriel.assistentefinanceiro.service.UsuarioService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DadosIniciaisConfig {

    @Bean
    ApplicationRunner dadosIniciais(
            JdbcTemplate jdbcTemplate,
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            atualizarSchemaUsuarios(jdbcTemplate);
            criptografarSenhasLegadas(usuarioRepository, passwordEncoder);
            usuarioRepository.findAll().forEach(usuarioService::criarCategoriasPadrao);
        };
    }

    private void atualizarSchemaUsuarios(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("alter table if exists usuarios add column if not exists email varchar(255)");
        jdbcTemplate.execute("alter table if exists usuarios add column if not exists senha varchar(255)");
        jdbcTemplate.execute("alter table if exists usuarios add column if not exists telefone_whatsapp varchar(255)");
        jdbcTemplate.execute("alter table if exists usuarios add column if not exists data_criacao timestamp");
        jdbcTemplate.execute("""
                update usuarios
                set email = concat('usuario', id, '@local.mvp')
                where email is null or email = ''
                """);
        jdbcTemplate.execute("""
                update usuarios
                set senha = '123456'
                where senha is null or senha = ''
                """);
        jdbcTemplate.execute("""
                do $$
                begin
                    if exists (
                        select 1
                        from information_schema.columns
                        where table_name = 'usuarios'
                        and column_name = 'telefone'
                    ) then
                        update usuarios
                        set telefone_whatsapp = telefone
                        where telefone_whatsapp is null;
                    end if;
                end $$;
                """);
        jdbcTemplate.execute("""
                update usuarios
                set telefone_whatsapp = concat('550000000', id)
                where telefone_whatsapp is null or telefone_whatsapp = ''
                """);
        jdbcTemplate.execute("""
                update usuarios
                set telefone_whatsapp = regexp_replace(telefone_whatsapp, '[whatsapp:\\+\\.\\s\\(\\)-]', '', 'g')
                where telefone_whatsapp is not null
                """);
        jdbcTemplate.execute("""
                update usuarios usuario
                set telefone_whatsapp = concat('55', usuario.telefone_whatsapp)
                where usuario.telefone_whatsapp ~ '^\\d{10,11}$'
                and not exists (
                    select 1
                    from usuarios outro_usuario
                    where outro_usuario.id <> usuario.id
                    and outro_usuario.telefone_whatsapp = concat('55', usuario.telefone_whatsapp)
                )
                """);
        jdbcTemplate.execute("""
                update usuarios
                set data_criacao = now()
                where data_criacao is null
                """);
        jdbcTemplate.execute("create unique index if not exists usuarios_email_idx on usuarios (email)");
        jdbcTemplate.execute("create unique index if not exists usuarios_telefone_whatsapp_idx on usuarios (telefone_whatsapp)");
    }

    private void criptografarSenhasLegadas(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getSenha() == null || !usuario.getSenha().startsWith("$2"))
                .forEach(usuario -> {
                    String senhaAtual = usuario.getSenha();
                    String senhaParaCriptografar = (senhaAtual == null || senhaAtual.isBlank()) ? "123456" : senhaAtual;
                    usuario.setSenha(passwordEncoder.encode(senhaParaCriptografar));
                    usuarioRepository.save(usuario);
                });
    }
}
