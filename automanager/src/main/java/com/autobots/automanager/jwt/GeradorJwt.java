package com.autobots.automanager.jwt;

import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class GeradorJwt {
	private String assinatura;
	private long duracao;

	public GeradorJwt(String assinatura, long duracao) {
		this.assinatura = assinatura;
		this.duracao = duracao;
	}

	public String gerarJwt(String nomeUsuario) {
		Date expiracao = new Date(System.currentTimeMillis() + this.duracao);
		String jwt = Jwts.builder()
				.setSubject(nomeUsuario)
				.setExpiration(expiracao)
				.signWith(SignatureAlgorithm.HS512, this.assinatura.getBytes())
				.compact();
		return jwt;
	}

	public String gerarJwt(String nomeUsuario, String perfil) {
		Date expiracao = new Date(System.currentTimeMillis() + this.duracao);
		String jwt = Jwts.builder()
				.setSubject(nomeUsuario)
				.claim("perfil", perfil)
				.setExpiration(expiracao)
				.signWith(SignatureAlgorithm.HS512, this.assinatura.getBytes())
				.compact();
		return jwt;
	}
}