package com.autobots.automanager.servicos;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autobots.automanager.dtos.Email.EmailAtualizarDTO;
import com.autobots.automanager.dtos.Email.EmailCadastrarDTO;
import com.autobots.automanager.dtos.Email.EmailExibirDTO;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.Email.EmailAtualizador;
import com.autobots.automanager.modelo.Email.EmailSelecionador;
import com.autobots.automanager.modelo.Usuario.UsuarioSelecionador;
import com.autobots.automanager.repositorios.EmailRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Service
public class EmailServico {

    @Autowired
    private EmailRepositorio repositorio;

    @Autowired
    private EmailSelecionador selecionador;

    @Autowired
    private EmailAtualizador atualizador;

    @Autowired
    private UsuarioSelecionador usuarioSelecionador;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    public void cadastrar(Email email) {
        repositorio.save(email);
    }

    public List<EmailExibirDTO> buscarTodos() {
        return usuarioRepositorio.findAll().stream()
                .flatMap(u -> u.getEmails().stream()
                        .map(e -> converterParaExibirDTO(e, u.getId())))
                .collect(Collectors.toList());
    }

    // MÉTODO NECESSÁRIO PARA O EmailControle
    public List<EmailExibirDTO> buscarPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioSelecionador.selecionar(usuarioId);
        return usuario.getEmails().stream()
                .map(e -> converterParaExibirDTO(e, usuario.getId()))
                .collect(Collectors.toList());
    }

    public EmailExibirDTO buscarPorIdDTO(Long id) {
        Email email = selecionador.selecionar(id);
        Long donoId = usuarioRepositorio.findAll().stream()
                .filter(u -> u.getEmails().contains(email))
                .map(Usuario::getId)
                .findFirst()
                .orElse(null);
        return converterParaExibirDTO(email, donoId);
    }

    public void atualizar(Email atualizacao) {
        Email email = selecionador.selecionar(atualizacao.getId());
        atualizador.atualizar(email, atualizacao);
        repositorio.save(email);
    }

    public void excluir(Long id) {
        Email email = selecionador.selecionar(id);
        repositorio.delete(email);
    }

    public EmailExibirDTO cadastrarViaDTO(EmailCadastrarDTO dto) {
        Usuario dono = usuarioSelecionador.selecionar(dto.getUsuarioId());
        Email email = new Email();
        email.setEndereco(dto.getEndereco());
        dono.getEmails().add(email);
        Usuario donoSalvo = usuarioRepositorio.save(dono);

        Email persistido = donoSalvo.getEmails().stream()
                .filter(e -> e.getEndereco().equals(dto.getEndereco()))
                .findFirst()
                .orElse(email);

        return converterParaExibirDTO(persistido, donoSalvo.getId());
    }

    public void atualizarViaDTO(EmailAtualizarDTO dto) {
        Email email = selecionador.selecionar(dto.getId());
        Email dadosAtualizacao = new Email();
        dadosAtualizacao.setEndereco(dto.getEndereco());
        atualizador.atualizar(email, dadosAtualizacao);
        repositorio.save(email);
    }

    private EmailExibirDTO converterParaExibirDTO(Email email, Long usuarioId) {
        EmailExibirDTO dto = new EmailExibirDTO();
        dto.setId(email.getId());
        dto.setEndereco(email.getEndereco());
        dto.setUsuarioId(usuarioId);
        return dto;
    }
}