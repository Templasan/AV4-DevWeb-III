package com.autobots.automanager.servicos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dtos.LoginDTO;
import com.autobots.automanager.dtos.LoginResponseDTO;
import com.autobots.automanager.dtos.Usuario.UsuarioExibirDTO;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.jwt.ProvedorJwt;
import com.autobots.automanager.repositorios.CredencialUsuarioSenhaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

import java.util.Date;
import java.util.stream.Collectors;

@Service
@Transactional
public class AutenticacaoServico {

    @Autowired
    private CredencialUsuarioSenhaRepositorio credencialRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private UsuarioServico usuarioServico;

    @Autowired
    private ProvedorJwt provedorJwt;

    public LoginResponseDTO login(LoginDTO loginDTO) {
        CredencialUsuarioSenha credencial = credencialRepositorio
                .findByNomeUsuario(loginDTO.getNomeUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuário ou senha inválidos"));

        if (credencial.isInativo()) {
            throw new IllegalArgumentException("Usuário inativo");
        }

        if (!credencial.getSenha().equals(loginDTO.getSenha())) {
            throw new IllegalArgumentException("Usuário ou senha inválidos");
        }

        credencial.setUltimoAcesso(new Date());
        credencialRepositorio.save(credencial);

        Usuario usuario = usuarioRepositorio.findAll().stream()
                .filter(u -> u.getCredenciais().contains(credencial))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        String perfil = usuario.getPerfis().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        String token = provedorJwt.proverJwt(loginDTO.getNomeUsuario(), perfil);

        UsuarioExibirDTO usuarioDTO = usuarioServico.buscarPorIdDTO(usuario.getId());
        return new LoginResponseDTO(usuarioDTO, token);
    }

    public Usuario validarToken(String token) {
        if (!provedorJwt.validarJwt(token)) {
            throw new IllegalArgumentException("Token inválido");
        }

        String nomeUsuario = provedorJwt.obterNomeUsuario(token);

        CredencialUsuarioSenha credencial = credencialRepositorio
                .findByNomeUsuario(nomeUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        Usuario usuario = usuarioRepositorio.findAll().stream()
                .filter(u -> u.getCredenciais().contains(credencial))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (credencial.isInativo()) {
            throw new IllegalArgumentException("Usuário inativo");
        }

        return usuario;
    }
}
