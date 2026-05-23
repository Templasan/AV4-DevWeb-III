package com.autobots.automanager.servicos;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dtos.Usuario.UsuarioAtualizarDTO;
import com.autobots.automanager.dtos.Usuario.UsuarioCadastrarDTO;
import com.autobots.automanager.dtos.Usuario.UsuarioExibirDTO;
import com.autobots.automanager.dtos.Email.EmailExibirDTO;
import com.autobots.automanager.dtos.Endereco.EnderecoExibirDTO;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.modelo.Empresa.EmpresaSelecionador;
import com.autobots.automanager.modelo.Usuario.UsuarioAtualizador;
import com.autobots.automanager.modelo.Usuario.UsuarioSelecionador;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Service
@Transactional
public class UsuarioServico {

    @Autowired
    private UsuarioRepositorio repositorio;

    @Autowired
    private UsuarioSelecionador selecionador;

    @Autowired
    private UsuarioAtualizador atualizador;

    @Autowired
    private EmpresaSelecionador empresaSelecionador;

    public void cadastrar(Usuario usuario) {
        repositorio.save(usuario);
    }

    public List<UsuarioExibirDTO> buscarTodos() {
        return repositorio.findAll().stream()
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    public UsuarioExibirDTO buscarPorIdDTO(Long id) {
        Usuario usuario = selecionador.selecionar(id);
        return converterParaExibirDTO(usuario);
    }

    public void atualizar(Usuario atualizacao) {
        Usuario usuario = selecionador.selecionar(atualizacao.getId());
        atualizador.atualizar(usuario, atualizacao);
        repositorio.save(usuario);
    }

    public void excluir(Long id) {
        Usuario usuario = selecionador.selecionar(id);
        repositorio.delete(usuario);
    }

    public List<UsuarioExibirDTO> buscarPorEmpresaETipo(Long empresaId, String tipoDesejado) {
        Empresa empresa = empresaSelecionador.selecionar(empresaId);
        return empresa.getUsuarios().stream()
                .filter(usuario -> usuario.getPerfis().stream()
                        .anyMatch(perfil -> perfil.name().equalsIgnoreCase(tipoDesejado)))
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    public UsuarioExibirDTO cadastrarViaDTO(UsuarioCadastrarDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setNomeSocial(dto.getNomeSocial());
        
        if (dto.getPerfis() != null) {
            usuario.getPerfis().addAll(dto.getPerfis());
        }
        
        // Resolvendo o BO do Endereço: Instanciando manualmente para garantir o Cascade
        if (dto.getEndereco() != null) {
            Endereco endereco = new Endereco();
            endereco.setEstado(dto.getEndereco().getEstado());
            endereco.setCidade(dto.getEndereco().getCidade());
            endereco.setBairro(dto.getEndereco().getBairro());
            endereco.setRua(dto.getEndereco().getRua());
            endereco.setNumero(dto.getEndereco().getNumero());
            endereco.setCodigoPostal(dto.getEndereco().getCodigoPostal());
            endereco.setInformacoesAdicionais(dto.getEndereco().getInformacoesAdicionais());
            usuario.setEndereco(endereco);
        }

        if (dto.getTelefones() != null) {
            usuario.getTelefones().addAll(dto.getTelefones());
        }

        // Resolvendo erro de 'data_emissao' cannot be null nos Documentos
        if (dto.getDocumentos() != null) {
            dto.getDocumentos().forEach(doc -> {
                if (doc.getDataEmissao() == null) {
                    doc.setDataEmissao(new Date()); 
                }
                usuario.getDocumentos().add(doc);
            });
        }

        if (dto.getEmails() != null) {
            usuario.getEmails().addAll(dto.getEmails());
        }

        // Fábrica de Credenciais (Abstract Factory para Credencial)
        if (dto.getCredenciais() != null) {
            dto.getCredenciais().forEach(cDto -> {
                if ("USUARIO_SENHA".equalsIgnoreCase(cDto.getTipo())) {
                    CredencialUsuarioSenha cred = new CredencialUsuarioSenha();
                    cred.setNomeUsuario(cDto.getNomeUsuario());
                    cred.setSenha(cDto.getSenha());
                    cred.setCriacao(new Date());
                    cred.setInativo(false);
                    usuario.getCredenciais().add(cred);
                } 
                else if ("CODIGO_BARRA".equalsIgnoreCase(cDto.getTipo())) {
                    CredencialCodigoBarra cred = new CredencialCodigoBarra();
                    cred.setCodigo(cDto.getCodigo() != null ? cDto.getCodigo() : 0L);
                    cred.setCriacao(new Date());
                    cred.setInativo(false);
                    usuario.getCredenciais().add(cred);
                }
            });
        }

        repositorio.save(usuario);
        return converterParaExibirDTO(usuario);
    }

    public void atualizarViaDTO(UsuarioAtualizarDTO dto) {
        Usuario usuario = selecionador.selecionar(dto.getId());
        Usuario dadosAtualizacao = new Usuario();
        dadosAtualizacao.setNome(dto.getNome());
        dadosAtualizacao.setNomeSocial(dto.getNomeSocial());

        atualizador.atualizar(usuario, dadosAtualizacao);
        repositorio.save(usuario);
    }

    private UsuarioExibirDTO converterParaExibirDTO(Usuario usuario) {
        UsuarioExibirDTO dto = new UsuarioExibirDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setNomeSocial(usuario.getNomeSocial());
        dto.setPerfis(usuario.getPerfis());


        if (usuario.getEndereco() != null) {
            EnderecoExibirDTO enderecoDto = new EnderecoExibirDTO();
            enderecoDto.setId(usuario.getEndereco().getId());
            enderecoDto.setEstado(usuario.getEndereco().getEstado());
            enderecoDto.setCidade(usuario.getEndereco().getCidade());
            enderecoDto.setBairro(usuario.getEndereco().getBairro());
            enderecoDto.setRua(usuario.getEndereco().getRua());
            enderecoDto.setNumero(usuario.getEndereco().getNumero());
            enderecoDto.setCodigoPostal(usuario.getEndereco().getCodigoPostal());
            enderecoDto.setInformacoesAdicionais(usuario.getEndereco().getInformacoesAdicionais());
            enderecoDto.setIdDono(usuario.getId()); // Seta o ID do dono para o HATEOAS

            dto.setEndereco(enderecoDto);
        }

        
        if (usuario.getEmails() != null) {
            dto.setEmails(usuario.getEmails().stream()
                    .map(email -> converterEmailParaDTO(email, usuario.getId()))
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    private EmailExibirDTO converterEmailParaDTO(Email email, Long usuarioId) {
        EmailExibirDTO dto = new EmailExibirDTO();
        dto.setId(email.getId());
        dto.setEndereco(email.getEndereco());
        dto.setUsuarioId(usuarioId);
        return dto;
    }
}