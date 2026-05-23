package com.autobots.automanager.adaptadores;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;

@SuppressWarnings("serial")
public class UserDetailsImpl implements UserDetails {
	private Usuario usuario;

	public UserDetailsImpl(Usuario usuario) {
		this.usuario = usuario;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<SimpleGrantedAuthority> autoridades = new ArrayList<>();
		for (PerfilUsuario perfil : usuario.getPerfis()) {
			autoridades.add(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
		}
		return autoridades;
	}

	@Override
	public String getPassword() {
		CredencialUsuarioSenha credencial = (CredencialUsuarioSenha) usuario.getCredenciais().stream()
				.filter(c -> c instanceof CredencialUsuarioSenha)
				.findFirst()
				.orElse(null);
		return credencial != null ? credencial.getSenha() : null;
	}

	@Override
	public String getUsername() {
		CredencialUsuarioSenha credencial = (CredencialUsuarioSenha) usuario.getCredenciais().stream()
				.filter(c -> c instanceof CredencialUsuarioSenha)
				.findFirst()
				.orElse(null);
		return credencial != null ? credencial.getNomeUsuario() : null;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	/** Expõe a entidade Usuario para uso em ContextoSeguranca e testes. */
	public Usuario getUsuario() {
		return this.usuario;
	}
}