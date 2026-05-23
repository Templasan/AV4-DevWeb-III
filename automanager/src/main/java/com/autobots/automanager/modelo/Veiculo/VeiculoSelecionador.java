package com.autobots.automanager.modelo.Veiculo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;

@Component
public class VeiculoSelecionador {
    @Autowired
    private VeiculoRepositorio repositorio;

    public Veiculo selecionar(Long id) {
        return repositorio.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Veículo não encontrado", "ID: " + id));
    }
}