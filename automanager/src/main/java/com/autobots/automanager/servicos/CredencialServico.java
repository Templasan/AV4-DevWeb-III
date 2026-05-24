package com.autobots.automanager.servicos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dtos.Credencial.CredencialInputDTO;
import com.autobots.automanager.dtos.Credencial.CredencialExibirDTO;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.modelo.Usuario.UsuarioSelecionador;
import com.autobots.automanager.repositorios.CredencialRepositorio;
import com.autobots.automanager.repositorios.CredencialUsuarioSenhaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CredencialServico {

    @Autowired
    private CredencialRepositorio credencialRepositorio;

    @Autowired
    private CredencialUsuarioSenhaRepositorio credencialUsuarioSenhaRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private UsuarioSelecionador usuarioSelecionador;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public CredencialExibirDTO adicionarCredencial(Long usuarioId, CredencialInputDTO dto) {
        Usuario usuario = usuarioSelecionador.selecionar(usuarioId);

        Credencial credencial = null;

        if ("USUARIO_SENHA".equalsIgnoreCase(dto.getTipo())) {
            // Valida se nomeUsuario já existe
            if (credencialUsuarioSenhaRepositorio.findByNomeUsuario(dto.getNomeUsuario()).isPresent()) {
                throw new IllegalArgumentException("Nome de usuário já existe");
            }

            CredencialUsuarioSenha cred = new CredencialUsuarioSenha();
            cred.setNomeUsuario(dto.getNomeUsuario());
            cred.setSenha(passwordEncoder.encode(dto.getSenha())); // Hash da senha
            cred.setCriacao(new Date());
            cred.setInativo(false);
            credencial = credencialRepositorio.save(cred);
        } else if ("CODIGO_BARRA".equalsIgnoreCase(dto.getTipo())) {
            CredencialCodigoBarra cred = new CredencialCodigoBarra();
            cred.setCodigo(dto.getCodigo() != null ? dto.getCodigo() : 0L);
            cred.setCriacao(new Date());
            cred.setInativo(false);
            credencial = credencialRepositorio.save(cred);
        } else {
            throw new IllegalArgumentException("Tipo de credencial inválido: " + dto.getTipo());
        }

        usuario.getCredenciais().add(credencial);
        usuarioRepositorio.save(usuario);

        return converterParaExibirDTO(credencial);
    }

    public CredencialExibirDTO atualizarCredencialUsuarioSenha(Long credencialId, String novaSenha) {
        CredencialUsuarioSenha credencial = credencialUsuarioSenhaRepositorio.findById(credencialId)
                .orElseThrow(() -> new IllegalArgumentException("Credencial não encontrada"));

        credencial.setSenha(passwordEncoder.encode(novaSenha)); // Hash da nova senha
        credencial.setUltimoAcesso(new Date());
        credencialUsuarioSenhaRepositorio.save(credencial);

        return converterParaExibirDTO(credencial);
    }

    public void desativarCredencial(Long credencialId) {
        Credencial credencial = credencialRepositorio.findById(credencialId)
                .orElseThrow(() -> new IllegalArgumentException("Credencial não encontrada"));

        credencial.setInativo(true);
        credencialRepositorio.save(credencial);
    }

    public void ativarCredencial(Long credencialId) {
        Credencial credencial = credencialRepositorio.findById(credencialId)
                .orElseThrow(() -> new IllegalArgumentException("Credencial não encontrada"));

        credencial.setInativo(false);
        credencialRepositorio.save(credencial);
    }

    public void removerCredencial(Long usuarioId, Long credencialId) {
        Usuario usuario = usuarioSelecionador.selecionar(usuarioId);
        Credencial credencial = credencialRepositorio.findById(credencialId)
                .orElseThrow(() -> new IllegalArgumentException("Credencial não encontrada"));

        usuario.getCredenciais().remove(credencial);
        usuarioRepositorio.save(usuario);
        credencialRepositorio.delete(credencial);
    }

    public List<CredencialExibirDTO> listarCredenciaisDoUsuario(Long usuarioId) {
        Usuario usuario = usuarioSelecionador.selecionar(usuarioId);
        return usuario.getCredenciais().stream()
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    private CredencialExibirDTO converterParaExibirDTO(Credencial credencial) {
        CredencialExibirDTO dto = new CredencialExibirDTO();
        dto.setId(credencial.getId());
        dto.setCriacao(credencial.getCriacao());
        dto.setUltimoAcesso(credencial.getUltimoAcesso());
        dto.setInativo(credencial.isInativo());

        if (credencial instanceof CredencialUsuarioSenha) {
            dto.setTipo("USUARIO_SENHA");
            dto.setNomeUsuario(((CredencialUsuarioSenha) credencial).getNomeUsuario());
        } else if (credencial instanceof CredencialCodigoBarra) {
            dto.setTipo("CODIGO_BARRA");
            dto.setCodigo(((CredencialCodigoBarra) credencial).getCodigo());
        }

        return dto;
    }
}
