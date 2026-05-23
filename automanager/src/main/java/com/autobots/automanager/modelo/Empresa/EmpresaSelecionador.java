package com.autobots.automanager.modelo.Empresa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;

@Component
public class EmpresaSelecionador {
    @Autowired
    private EmpresaRepositorio repositorio;

    public Empresa selecionar(Long id) {
        return repositorio.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Empresa não encontrada", "ID: " + id));
    }
}