package com.autobots.automanager.servicos;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dtos.Mercadoria.*;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.modelo.Mercadoria.MercadoriaAtualizador;
import com.autobots.automanager.modelo.Mercadoria.MercadoriaSelecionador;
import com.autobots.automanager.modelo.Usuario.UsuarioSelecionador;
import com.autobots.automanager.modelo.Empresa.EmpresaSelecionador;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.EmpresaRepositorio;

@Service
@Transactional
public class MercadoriaServico {

    @Autowired
    private MercadoriaRepositorio repositorio;

    @Autowired
    private MercadoriaSelecionador selecionador;

    @Autowired
    private MercadoriaAtualizador atualizador;

    @Autowired
    private UsuarioSelecionador usuarioSelecionador;

    @Autowired
    private EmpresaSelecionador empresaSelecionador;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    public List<MercadoriaExibirDTO> buscarTodos() {
        return repositorio.findAll().stream()
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    public MercadoriaExibirDTO buscarPorIdDTO(Long id) {
        Mercadoria mercadoria = selecionador.selecionar(id);
        return converterParaExibirDTO(mercadoria);
    }

    public List<MercadoriaExibirDTO> buscarPorFornecedor(Long usuarioId) {
        Usuario fornecedor = usuarioSelecionador.selecionar(usuarioId);
        return fornecedor.getMercadorias().stream()
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    public List<MercadoriaExibirDTO> buscarPorEmpresa(Long empresaId) {
        Empresa empresa = empresaSelecionador.selecionar(empresaId);
        return empresa.getMercadorias().stream()
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    public MercadoriaExibirDTO cadastrarViaDTO(MercadoriaCadastrarDTO dto) {
        Mercadoria mercadoria = new Mercadoria();
        mercadoria.setNome(dto.getNome());
        mercadoria.setDescricao(dto.getDescricao());
        mercadoria.setQuantidade(dto.getQuantidade());
        mercadoria.setValor(dto.getValor());
        mercadoria.setValidade(dto.getValidade());
        mercadoria.setFabricacao(dto.getFabricacao());
        mercadoria.setCadastro(new java.util.Date());

        // A mercadoria é salva primeiro para gerar o ID
        repositorio.save(mercadoria);

        // Vincula ao Fornecedor se houver ID
        if (dto.getIdFornecedor() != null) {
            Usuario fornecedor = usuarioSelecionador.selecionar(dto.getIdFornecedor());
            fornecedor.getMercadorias().add(mercadoria);
            usuarioRepositorio.save(fornecedor);
        }
        
        // Se o DTO permitir idEmpresa, você pode adicionar a lógica aqui também
        // if (dto.getIdEmpresa() != null) { ... }

        return converterParaExibirDTO(mercadoria);
    }

    public void atualizarViaDTO(MercadoriaAtualizarDTO dto) {
        Mercadoria mercadoria = selecionador.selecionar(dto.getId());
        Mercadoria dadosAtualizacao = new Mercadoria();
        dadosAtualizacao.setNome(dto.getNome());
        dadosAtualizacao.setDescricao(dto.getDescricao());
        if (dto.getQuantidade() != null) {
            dadosAtualizacao.setQuantidade(dto.getQuantidade());
        }
        if (dto.getValor() != null) {
            dadosAtualizacao.setValor(dto.getValor());
        }
        dadosAtualizacao.setValidade(dto.getValidade());
        dadosAtualizacao.setFabricacao(dto.getFabricacao());

        atualizador.atualizar(mercadoria, dadosAtualizacao);
        repositorio.save(mercadoria);
    }

    public void excluir(Long id) {
        Mercadoria mercadoria = selecionador.selecionar(id);
        
        // Remove vínculos em cascata manual para evitar erros de FK
        usuarioRepositorio.findAll().stream()
            .filter(u -> u.getMercadorias().contains(mercadoria))
            .forEach(u -> {
                u.getMercadorias().remove(mercadoria);
                usuarioRepositorio.save(u);
            });

        empresaRepositorio.findAll().stream()
            .filter(e -> e.getMercadorias().contains(mercadoria))
            .forEach(e -> {
                e.getMercadorias().remove(mercadoria);
                empresaRepositorio.save(e);
            });
        
        repositorio.delete(mercadoria);
    }

    private MercadoriaExibirDTO converterParaExibirDTO(Mercadoria m) {
        MercadoriaExibirDTO dto = new MercadoriaExibirDTO();
        dto.setId(m.getId());
        dto.setNome(m.getNome());
        dto.setDescricao(m.getDescricao());
        dto.setQuantidade(m.getQuantidade());
        dto.setValor(m.getValor());
        dto.setValidade(m.getValidade());
        dto.setFabricacao(m.getFabricacao());
        dto.setCadastro(m.getCadastro());

        // Identificação do Fornecedor para os Links HATEOAS
        usuarioRepositorio.findAll().stream()
            .filter(u -> u.getMercadorias().contains(m))
            .findFirst()
            .ifPresent(u -> dto.setIdFornecedor(u.getId()));

        // Identificação da Empresa para os Links HATEOAS
        empresaRepositorio.findAll().stream()
            .filter(e -> e.getMercadorias().contains(m))
            .findFirst()
            .ifPresent(e -> dto.setIdEmpresa(e.getId()));

        return dto;
    }
}