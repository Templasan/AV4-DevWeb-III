package com.autobots.automanager.modelo.Usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;

@Component
public class UsuarioSelecionador {
    @Autowired
    private UsuarioRepositorio repositorio;

    public Usuario selecionar(Long id) {
        return repositorio.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado", "ID: " + id));
    }
}