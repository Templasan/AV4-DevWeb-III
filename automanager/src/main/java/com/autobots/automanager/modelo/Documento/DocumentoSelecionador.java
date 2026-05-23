package com.autobots.automanager.modelo.Documento;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.repositorios.DocumentoRepositorio;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;

@Component
public class DocumentoSelecionador {

	@Autowired
	private DocumentoRepositorio repositorio;

	public List<Documento> selecionarTodos() {
		return repositorio.findAll();
	}

	public Documento selecionar(Long id) {
		return repositorio.findById(id)
				.orElseThrow(() -> new EntidadeNaoEncontradaException("Documento não encontrado", "Nenhum documento com o ID: " + id));
	}
}