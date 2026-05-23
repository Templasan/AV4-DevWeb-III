package com.autobots.automanager.servicos;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autobots.automanager.dtos.Telefone.TelefoneAtualizarDTO;
import com.autobots.automanager.dtos.Telefone.TelefoneCadastrarDTO;
import com.autobots.automanager.dtos.Telefone.TelefoneExibirDTO;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;
import com.autobots.automanager.modelo.Telefone.TelefoneAtualizador;
import com.autobots.automanager.modelo.Telefone.TelefoneSelecionador;
import com.autobots.automanager.modelo.Usuario.UsuarioSelecionador;
import com.autobots.automanager.modelo.Empresa.EmpresaSelecionador;
import com.autobots.automanager.repositorios.TelefoneRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.EmpresaRepositorio;

@Service
public class TelefoneServico {
    
    @Autowired
    private TelefoneRepositorio repositorio;
    @Autowired
    private TelefoneSelecionador selecionador;
    @Autowired
    private TelefoneAtualizador atualizador;
    @Autowired
    private UsuarioSelecionador usuarioSelecionador;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private EmpresaSelecionador empresaSelecionador;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    public List<TelefoneExibirDTO> buscarTodos() {
        List<TelefoneExibirDTO> todosTelefones = new ArrayList<>();
        usuarioRepositorio.findAll().forEach(u -> 
            u.getTelefones().forEach(t -> todosTelefones.add(converterParaExibirDTO(t, u.getId())))
        );
        empresaRepositorio.findAll().forEach(e -> 
            e.getTelefones().forEach(t -> todosTelefones.add(converterParaExibirDTO(t, e.getId())))
        );
        return todosTelefones;
    }

    public TelefoneExibirDTO buscarPorIdDTO(Long id) {
        Telefone telefone = selecionador.selecionar(id);
        Long donoId = usuarioRepositorio.findAll().stream()
                .filter(u -> u.getTelefones().contains(telefone))
                .map(Usuario::getId).findFirst()
                .orElseGet(() -> empresaRepositorio.findAll().stream()
                        .filter(e -> e.getTelefones().contains(telefone))
                        .map(Empresa::getId).findFirst().orElse(null));
        return converterParaExibirDTO(telefone, donoId);
    }

    // NOVO: Método para buscar telefones de um Usuário específico
    public List<TelefoneExibirDTO> buscarPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioSelecionador.selecionar(usuarioId);
        return usuario.getTelefones().stream()
                .map(t -> converterParaExibirDTO(t, usuario.getId()))
                .collect(Collectors.toList());
    }

    // NOVO: Método para buscar telefones de uma Empresa específica
    public List<TelefoneExibirDTO> buscarPorEmpresa(Long empresaId) {
        Empresa empresa = empresaSelecionador.selecionar(empresaId);
        return empresa.getTelefones().stream()
                .map(t -> converterParaExibirDTO(t, empresa.getId()))
                .collect(Collectors.toList());
    }

    public TelefoneExibirDTO cadastrarViaDTO(TelefoneCadastrarDTO dto) {
        Telefone novoTelefone = new Telefone();
        novoTelefone.setDdd(dto.getDdd());
        novoTelefone.setNumero(dto.getNumero());

        if ("USUARIO".equalsIgnoreCase(dto.getTipoDono())) {
            Usuario dono = usuarioSelecionador.selecionar(dto.getIdDono());
            dono.getTelefones().add(novoTelefone);
            usuarioRepositorio.save(dono);
            return converterParaExibirDTO(novoTelefone, dono.getId());
        } else if ("EMPRESA".equalsIgnoreCase(dto.getTipoDono())) {
            Empresa empresa = empresaSelecionador.selecionar(dto.getIdDono());
            empresa.getTelefones().add(novoTelefone);
            empresaRepositorio.save(empresa);
            return converterParaExibirDTO(novoTelefone, empresa.getId());
        }
        throw new RuntimeException("Tipo de dono inválido para telefone.");
    }

    public void atualizarViaDTO(TelefoneAtualizarDTO dto) {
        Telefone telefone = selecionador.selecionar(dto.getId());
        Telefone dados = new Telefone();
        dados.setDdd(dto.getDdd());
        dados.setNumero(dto.getNumero());
        atualizador.atualizar(telefone, dados);
        repositorio.save(telefone);
    }

    public void excluir(Long idTelefone, Long idDono) {
        Usuario usuario = usuarioRepositorio.findById(idDono).orElse(null);
        if (usuario != null) {
            if (usuario.getTelefones().removeIf(t -> t.getId().equals(idTelefone))) {
                usuarioRepositorio.save(usuario);
                return;
            }
        }
        Empresa empresa = empresaRepositorio.findById(idDono).orElse(null);
        if (empresa != null) {
            if (empresa.getTelefones().removeIf(t -> t.getId().equals(idTelefone))) {
                empresaRepositorio.save(empresa);
                return;
            }
        }
        throw new EntidadeNaoEncontradaException("Telefone ou vínculo não encontrado.");
    }

    private TelefoneExibirDTO converterParaExibirDTO(Telefone telefone, Long idDono) {
        TelefoneExibirDTO dto = new TelefoneExibirDTO();
        dto.setId(telefone.getId());
        dto.setDdd(telefone.getDdd());
        dto.setNumero(telefone.getNumero());
        dto.setIdDono(idDono);
        return dto;
    }
}