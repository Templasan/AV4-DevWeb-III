package com.autobots.automanager.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.CredencialUsuarioSenha;

import java.util.Optional;

public interface CredencialUsuarioSenhaRepositorio extends JpaRepository<CredencialUsuarioSenha, Long> {
    Optional<CredencialUsuarioSenha> findByNomeUsuario(String nomeUsuario);
}
