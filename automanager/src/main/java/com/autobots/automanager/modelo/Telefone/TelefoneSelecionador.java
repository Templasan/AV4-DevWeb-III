package com.autobots.automanager.modelo.Telefone;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;
import com.autobots.automanager.repositorios.TelefoneRepositorio;


@Component
public class TelefoneSelecionador {
    @Autowired
	private TelefoneRepositorio repositorio;

	public List<Telefone> selecionarTodos() {
		return repositorio.findAll();
	}

	public Telefone selecionar(Long id) {
		return repositorio.findById(id)
				.orElseThrow(() -> new EntidadeNaoEncontradaException("Telefone não encontrado", "Nenhum telefone com o ID: " + id));
	}
}
