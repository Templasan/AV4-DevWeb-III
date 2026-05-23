package com.autobots.automanager.modelo.Telefone;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.modelo.StringVerificadorNulo;

@Component
public class TelefoneAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Telefone telefone, Telefone atualizacao) {
		if (atualizacao != null) {
			if (!verificador.verificar(atualizacao.getDdd())) {
				telefone.setDdd(atualizacao.getDdd());
			}
			if (!verificador.verificar(atualizacao.getNumero())) {
				telefone.setNumero(atualizacao.getNumero());
			}
		}
	}

	public void atualizar(List<Telefone> telefones, List<Telefone> atualizacoes) {

		if (atualizacoes == null || atualizacoes.isEmpty()) {
        	return;
    	}	
		
		for (Telefone atualizacao : atualizacoes) {
			for (Telefone telefone : telefones) {
				if (atualizacao.getId() != null && telefone.getId() != null) {
					if (atualizacao.getId().equals(telefone.getId())) {
						atualizar(telefone, atualizacao);
					}
				}
			}
		}
	}
}