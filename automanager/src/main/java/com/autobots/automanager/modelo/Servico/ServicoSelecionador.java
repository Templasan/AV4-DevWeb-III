package com.autobots.automanager.modelo.Servico;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;

@Component
public class ServicoSelecionador {
    @Autowired
    private ServicoRepositorio repositorio;

    public Servico selecionar(Long id) {
        return repositorio.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Serviço não encontrado", "ID: " + id));
    }
}