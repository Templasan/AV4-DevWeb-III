package com.autobots.automanager.adaptadores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UsuarioRepositorio repositorio;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = repositorio.findAll().stream()
				.filter(u -> u.getCredenciais().stream()
						.anyMatch(c -> c instanceof CredencialUsuarioSenha &&
								((CredencialUsuarioSenha) c).getNomeUsuario().equals(username)))
				.findFirst()
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
		return new UserDetailsImpl(usuario);
	}
}