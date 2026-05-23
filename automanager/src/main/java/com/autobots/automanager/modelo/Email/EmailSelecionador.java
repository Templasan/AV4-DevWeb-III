package com.autobots.automanager.modelo.Email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.repositorios.EmailRepositorio;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;

@Component
public class EmailSelecionador {
    @Autowired
    private EmailRepositorio repositorio;

    public Email selecionar(Long id) {
        return repositorio.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Email não encontrado", "ID: " + id));
    }
}