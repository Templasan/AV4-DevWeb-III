package com.autobots.automanager.modelo.Venda;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.repositorios.VendaRepositorio;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;

@Component
public class VendaSelecionador {
    @Autowired
    private VendaRepositorio repositorio;

    public Venda selecionar(Long id) {
        return repositorio.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Venda não encontrada", "ID: " + id));
    }
}