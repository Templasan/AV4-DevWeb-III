package com.autobots.automanager.configuracao;

import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

/**
 * Insere usuários de teste com suas credenciais ao iniciar a aplicação.
 * Cada usuário representa um perfil diferente.
 */
@Component
public class DadosPrecargados implements CommandLineRunner {

	private final UsuarioRepositorio usuarioRepositorio;

	public DadosPrecargados(UsuarioRepositorio usuarioRepositorio) {
		this.usuarioRepositorio = usuarioRepositorio;
	}

	@Override
	public void run(String... args) throws Exception {
		// Verificar se já existem dados
		if (usuarioRepositorio.count() > 0) {
			return; // Dados já foram inseridos
		}

		Date agora = new Date();

		// Criar ADMIN
		Usuario admin = new Usuario();
		admin.setNome("Administrador Sistema");
		CredencialUsuarioSenha credAdmin = new CredencialUsuarioSenha();
		credAdmin.setNomeUsuario("admin");
		credAdmin.setSenha("admin123");
		credAdmin.setCriacao(agora);
		credAdmin.setInativo(false);
		admin.getCredenciais().add(credAdmin);
		admin.getPerfis().add(PerfilUsuario.ADMIN);
		usuarioRepositorio.save(admin);

		// Criar GERENTE
		Usuario gerente = new Usuario();
		gerente.setNome("Gerente Loja");
		CredencialUsuarioSenha credGerente = new CredencialUsuarioSenha();
		credGerente.setNomeUsuario("gerente");
		credGerente.setSenha("gerente123");
		credGerente.setCriacao(agora);
		credGerente.setInativo(false);
		gerente.getCredenciais().add(credGerente);
		gerente.getPerfis().add(PerfilUsuario.GERENTE);
		usuarioRepositorio.save(gerente);

		// Criar VENDEDOR
		Usuario vendedor = new Usuario();
		vendedor.setNome("João Vendedor");
		CredencialUsuarioSenha credVendedor = new CredencialUsuarioSenha();
		credVendedor.setNomeUsuario("vendedor");
		credVendedor.setSenha("vendedor123");
		credVendedor.setCriacao(agora);
		credVendedor.setInativo(false);
		vendedor.getCredenciais().add(credVendedor);
		vendedor.getPerfis().add(PerfilUsuario.VENDEDOR);
		usuarioRepositorio.save(vendedor);

		// Criar CLIENTE
		Usuario cliente = new Usuario();
		cliente.setNome("Maria Cliente");
		CredencialUsuarioSenha credCliente = new CredencialUsuarioSenha();
		credCliente.setNomeUsuario("cliente");
		credCliente.setSenha("cliente123");
		credCliente.setCriacao(agora);
		credCliente.setInativo(false);
		cliente.getCredenciais().add(credCliente);
		cliente.getPerfis().add(PerfilUsuario.CLIENTE);
		usuarioRepositorio.save(cliente);

		System.out.println("\n" + "=".repeat(60));
		System.out.println("✓ Dados de teste inseridos com sucesso!");
		System.out.println("=".repeat(60));
		System.out.println("  ADMIN:    login: admin     | senha: admin123");
		System.out.println("  GERENTE:  login: gerente   | senha: gerente123");
		System.out.println("  VENDEDOR: login: vendedor  | senha: vendedor123");
		System.out.println("  CLIENTE:  login: cliente   | senha: cliente123");
		System.out.println("=".repeat(60) + "\n");
	}
}
