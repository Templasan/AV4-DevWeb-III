package com.autobots.automanager.seguranca;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.repositorios.VendaRepositorio;

@Component("segurancaUtil")
public class SegurancaUtil {

	@Autowired
	private VendaRepositorio vendaRepositorio;

	/** Qualquer usuário pode ver seus próprios dados */
	public boolean isProprioUsuario(Long usuarioId) {
		Usuario logado = ContextoSeguranca.getUsuario();
		return logado != null && logado.getId().equals(usuarioId);
	}

	/**
	 * CLIENTE vê venda em que foi comprador.
	 * VENDEDOR vê venda em que foi funcionário.
	 * Usado em GET /vendas/{id}.
	 */
	public boolean isParticipanteVenda(Long vendaId) {
		Usuario logado = ContextoSeguranca.getUsuario();
		if (logado == null) return false;
		return vendaRepositorio.findById(vendaId)
				.map(v -> (v.getCliente() != null && logado.getId().equals(v.getCliente().getId()))
						|| (v.getFuncionario() != null && logado.getId().equals(v.getFuncionario().getId())))
				.orElse(false);
	}

	/**
	 * Verifica se o usuário logado é o VENDEDOR (funcionário) que registrou a venda.
	 * Usado em PUT/DELETE /vendas/{id} e sub-rotas de itens.
	 */
	public boolean isVendedorDaVenda(Long vendaId) {
		Usuario logado = ContextoSeguranca.getUsuario();
		if (logado == null) return false;
		return vendaRepositorio.findById(vendaId)
				.map(v -> v.getFuncionario() != null && logado.getId().equals(v.getFuncionario().getId()))
				.orElse(false);
	}
}
