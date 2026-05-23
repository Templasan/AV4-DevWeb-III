package com.autobots.automanager.servicos;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;

import com.autobots.automanager.dtos.Documento.DocumentoAtualizarDTO;
import com.autobots.automanager.dtos.Documento.DocumentoCadastrarDTO;
import com.autobots.automanager.dtos.Documento.DocumentoExibirDTO;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;
import com.autobots.automanager.modelo.Documento.DocumentoAtualizador;
import com.autobots.automanager.modelo.Documento.DocumentoSelecionador;
import com.autobots.automanager.modelo.Usuario.UsuarioSelecionador;
import com.autobots.automanager.repositorios.DocumentoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.seguranca.ContextoSeguranca;

@Service
public class DocumentoServico {
    
    @Autowired
    private DocumentoRepositorio repositorio;

    @Autowired
    private DocumentoSelecionador selecionador;

    @Autowired
    private DocumentoAtualizador atualizador;

    @Autowired
    private UsuarioSelecionador usuarioSelecionador;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    public void cadastrar(Documento documento) {
        repositorio.save(documento);
    }

    public List<DocumentoExibirDTO> buscarTodos() {
        Map<Documento, Usuario> documentoPorUsuario = new java.util.HashMap<>();
        List<Usuario> usuarios = usuarioRepositorio.findAll();
        
        for (Usuario usuario : usuarios) {
            for (Documento documento : usuario.getDocumentos()) {
                documentoPorUsuario.put(documento, usuario);
            }
        }
        
        return documentoPorUsuario.entrySet().stream()
            .map(entry -> converterParaExibirDTO(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    // MÉTODO NOVO PARA O CONTROLE
    public List<DocumentoExibirDTO> buscarPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioSelecionador.selecionar(usuarioId);
        return usuario.getDocumentos().stream()
                .map(documento -> converterParaExibirDTO(documento, usuario))
                .collect(Collectors.toList());
    }

    public DocumentoExibirDTO buscarPorIdDTO(Long id) {
        Documento documento = selecionador.selecionar(id);
        
        Usuario usuario = usuarioRepositorio.findAll().stream()
                .filter(u -> u.getDocumentos().stream()
                        .anyMatch(d -> d.getId().equals(id)))
                .findFirst()
                .orElse(null);
        
        return converterParaExibirDTO(documento, usuario);
    }

    public void atualizar(Documento atualizacao) {
        Documento documento = selecionador.selecionar(atualizacao.getId());
        atualizador.atualizar(documento, atualizacao);
        repositorio.save(documento);
    }

    public void excluir(Long idDocumento) {
    // 1. Localiza o documento no banco
    Documento documento = repositorio.findById(idDocumento)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Documento não encontrado."));

    // 2. Busca o Usuário que possui esse documento na lista dele
    // Usamos o stream para encontrar o dono sem precisar receber o idUsuario por parâmetro
    Usuario dono = usuarioRepositorio.findAll().stream()
            .filter(u -> u.getDocumentos().contains(documento))
            .findFirst()
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Nenhum usuário vinculado a este documento foi encontrado."));

    // 3. Remove o documento da lista do dono e salva o dono
    // Isso garante que a tabela de relacionamento seja limpa
    dono.getDocumentos().remove(documento);
    usuarioRepositorio.save(dono);

    // 4. Deleta o registro do documento do banco de vez
    repositorio.delete(documento);
    }

    
    public DocumentoExibirDTO cadastrarViaDTO(DocumentoCadastrarDTO dto) {
        Usuario dono = usuarioSelecionador.selecionar(dto.getUsuarioId());

        Usuario logado = ContextoSeguranca.getUsuario();
        if (logado != null && logado.getPerfis().contains(PerfilUsuario.VENDEDOR)) {
            boolean ehSiMesmo = logado.getId().equals(dono.getId());
            boolean ehCliente = dono.getPerfis().contains(PerfilUsuario.CLIENTE);
            if (!ehSiMesmo && !ehCliente) {
                throw new AccessDeniedException("Vendedor só pode gerenciar documentos de clientes ou de si mesmo.");
            }
        }

        Documento novoDocumento = new Documento();
        novoDocumento.setTipo(dto.getTipo());
        novoDocumento.setNumero(dto.getNumero());
        novoDocumento.setDataEmissao(dto.getDataEmissao() != null ? dto.getDataEmissao() : new java.util.Date());

        dono.getDocumentos().add(novoDocumento);
        Usuario donoSalvo = usuarioRepositorio.save(dono);

        Documento documentoPersistido = donoSalvo.getDocumentos().stream()
                .filter(d -> d.getNumero().equals(dto.getNumero()) && d.getTipo().equals(dto.getTipo()))
                .findFirst()
                .orElse(novoDocumento);

        return converterParaExibirDTO(documentoPersistido, donoSalvo);
    }

    public void atualizarViaDTO(DocumentoAtualizarDTO dto) {
        Documento documento = selecionador.selecionar(dto.getId());

        Usuario logado = ContextoSeguranca.getUsuario();
        if (logado != null && logado.getPerfis().contains(PerfilUsuario.VENDEDOR)) {
            Usuario donoDoc = usuarioRepositorio.findAll().stream()
                    .filter(u -> u.getDocumentos().stream().anyMatch(d -> d.getId().equals(dto.getId())))
                    .findFirst().orElse(null);
            if (donoDoc != null) {
                boolean ehSiMesmo = logado.getId().equals(donoDoc.getId());
                boolean ehCliente = donoDoc.getPerfis().contains(PerfilUsuario.CLIENTE);
                if (!ehSiMesmo && !ehCliente) {
                    throw new AccessDeniedException("Vendedor só pode gerenciar documentos de clientes ou de si mesmo.");
                }
            }
        }

        Documento dadosAtualizacao = new Documento();
        dadosAtualizacao.setNumero(dto.getNumero());
        dadosAtualizacao.setTipo(dto.getTipo());
        dadosAtualizacao.setDataEmissao(dto.getDataEmissao());

        atualizador.atualizar(documento, dadosAtualizacao);
        repositorio.save(documento);
    }

    public DocumentoExibirDTO converterParaExibirDTO(Documento documento, Usuario usuario) {
        DocumentoExibirDTO dto = new DocumentoExibirDTO();
        dto.setId(documento.getId());
        dto.setNumero(documento.getNumero());
        dto.setTipo(documento.getTipo());
        dto.setDataEmissao(documento.getDataEmissao());

        if (usuario != null) {
            dto.setIdUsuario(usuario.getId());
        }

        return dto;
    }
}