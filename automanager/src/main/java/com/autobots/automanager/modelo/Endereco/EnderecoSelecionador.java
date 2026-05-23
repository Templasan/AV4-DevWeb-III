package com.autobots.automanager.modelo.Endereco;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.repositorios.EnderecoRepositorio;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;

@Component
public class EnderecoSelecionador {
    @Autowired
	private EnderecoRepositorio repositorio;

	public List<Endereco> selecionarTodos() {
		return repositorio.findAll();
	}

	public Endereco selecionar(Long id) {
		return repositorio.findById(id)
				.orElseThrow(() -> new EntidadeNaoEncontradaException("Endereco não encontrado", "Nenhum endereço com o ID: " + id));
	}
}
