package com.autobots.automanager.modelo.Mercadoria;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;

@Component
public class MercadoriaSelecionador {
    @Autowired
    private MercadoriaRepositorio repositorio;

    public Mercadoria selecionar(Long id) {
        return repositorio.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Mercadoria não encontrada", "ID: " + id));
    }
}