package com.autobots.automanager.servicos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dtos.LoginDTO;
import com.autobots.automanager.dtos.LoginResponseDTO;
import com.autobots.automanager.dtos.Usuario.UsuarioExibirDTO;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.Usuario.UsuarioSelecionador;
import com.autobots.automanager.repositorios.CredencialUsuarioSenhaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class AutenticacaoServico {

    @Autowired
    private CredencialUsuarioSenhaRepositorio credencialRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private UsuarioSelecionador usuarioSelecionador;

    @Autowired
    private UsuarioServico usuarioServico;

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

        // Atualiza último acesso
        credencial.setUltimoAcesso(new Date());
        credencialRepositorio.save(credencial);

        // Busca o usuário associado
        Usuario usuario = usuarioRepositorio.findAll().stream()
                .filter(u -> u.getCredenciais().contains(credencial))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Gera token simples: Base64(nomeUsuario:UUID)
        String tokenRaw = loginDTO.getNomeUsuario() + ":" + UUID.randomUUID().toString();
        String token = Base64.getEncoder().encodeToString(tokenRaw.getBytes());

        UsuarioExibirDTO usuarioDTO = usuarioServico.buscarPorIdDTO(usuario.getId());
        return new LoginResponseDTO(usuarioDTO, token);
    }

    public Usuario validarToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String nomeUsuario = decoded.split(":")[0];

            CredencialUsuarioSenha credencial = credencialRepositorio
                    .findByNomeUsuario(nomeUsuario)
                    .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

            if (credencial.isInativo()) {
                throw new IllegalArgumentException("Usuário inativo");
            }

            return usuarioRepositorio.findAll().stream()
                    .filter(u -> u.getCredenciais().contains(credencial))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Token inválido");
        }
    }
}
