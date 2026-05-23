package com.autobots.automanager.servicos;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;

import com.autobots.automanager.dtos.Endereco.EnderecoAtualizarDTO;
import com.autobots.automanager.dtos.Endereco.EnderecoCadastrarDTO;
import com.autobots.automanager.dtos.Endereco.EnderecoExibirDTO;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.excecoes.personalizado.EntidadeNaoEncontradaException;
import com.autobots.automanager.modelo.Usuario.UsuarioSelecionador;
import com.autobots.automanager.modelo.Empresa.EmpresaSelecionador;
import com.autobots.automanager.modelo.Endereco.EnderecoAtualizador;
import com.autobots.automanager.modelo.Endereco.EnderecoSelecionador;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;
import com.autobots.automanager.seguranca.ContextoSeguranca;

@Service
public class EnderecoServico {

    @Autowired
    private EnderecoRepositorio repositorio;
    @Autowired
    private EnderecoSelecionador selecionador;
    @Autowired
    private EnderecoAtualizador atualizador;
    @Autowired
    private UsuarioSelecionador usuarioSelecionador;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private EmpresaSelecionador empresaSelecionador;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    public List<EnderecoExibirDTO> buscarTodos() {
        List<EnderecoExibirDTO> lista = usuarioRepositorio.findAll().stream()
                .filter(u -> u.getEndereco() != null)
                .map(u -> converterParaExibirDTO(u.getEndereco(), u.getId()))
                .collect(Collectors.toList());
        
        lista.addAll(empresaRepositorio.findAll().stream()
                .filter(e -> e.getEndereco() != null)
                .map(e -> converterParaExibirDTO(e.getEndereco(), e.getId()))
                .collect(Collectors.toList()));
        
        return lista;
    }

    // NOVO: Método para a rota GET /enderecos/{id}
    public EnderecoExibirDTO buscarPorIdDTO(Long id) {
        Endereco endereco = selecionador.selecionar(id);
        
        // Tenta descobrir o dono para preencher o DTO corretamente
        Long donoId = usuarioRepositorio.findAll().stream()
                .filter(u -> u.getEndereco() != null && u.getEndereco().getId().equals(id))
                .map(Usuario::getId).findFirst()
                .orElseGet(() -> empresaRepositorio.findAll().stream()
                        .filter(e -> e.getEndereco() != null && e.getEndereco().getId().equals(id))
                        .map(Empresa::getId).findFirst().orElse(null));

        return converterParaExibirDTO(endereco, donoId);
    }

    // NOVO: Método para a rota GET /enderecos/usuario/{usuarioId}
    public EnderecoExibirDTO buscarPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioSelecionador.selecionar(usuarioId);
        if (usuario.getEndereco() == null) {
            throw new EntidadeNaoEncontradaException("Este usuário não possui endereço cadastrado.");
        }
        return converterParaExibirDTO(usuario.getEndereco(), usuario.getId());
    }

    // NOVO: Método para a rota GET /enderecos/empresa/{empresaId}
    public EnderecoExibirDTO buscarPorEmpresa(Long empresaId) {
        Empresa empresa = empresaSelecionador.selecionar(empresaId);
        if (empresa.getEndereco() == null) {
            throw new EntidadeNaoEncontradaException("Esta empresa não possui endereço cadastrado.");
        }
        return converterParaExibirDTO(empresa.getEndereco(), empresa.getId());
    }

    public EnderecoExibirDTO cadastrarViaDTO(EnderecoCadastrarDTO dto) {
        Endereco endereco = new Endereco();
        preencherDados(endereco, dto);

        Usuario logado = ContextoSeguranca.getUsuario();
        if (logado != null && logado.getPerfis().contains(PerfilUsuario.VENDEDOR)) {
            if ("EMPRESA".equalsIgnoreCase(dto.getTipoDono())) {
                throw new AccessDeniedException("Vendedor não pode gerenciar endereços de empresas.");
            }
            if ("USUARIO".equalsIgnoreCase(dto.getTipoDono())) {
                Usuario alvo = usuarioSelecionador.selecionar(dto.getIdDono());
                boolean ehSiMesmo = logado.getId().equals(alvo.getId());
                boolean ehCliente = alvo.getPerfis().contains(PerfilUsuario.CLIENTE);
                if (!ehSiMesmo && !ehCliente) {
                    throw new AccessDeniedException("Vendedor só pode gerenciar endereços de clientes ou de si mesmo.");
                }
            }
        }

        if ("USUARIO".equalsIgnoreCase(dto.getTipoDono())) {
            Usuario dono = usuarioSelecionador.selecionar(dto.getIdDono());
            if (dono.getEndereco() != null) {
                throw new RuntimeException("Usuário já possui endereço. Use a atualização.");
            }
            dono.setEndereco(endereco);
            usuarioRepositorio.save(dono);
            return converterParaExibirDTO(dono.getEndereco(), dono.getId());

        } else if ("EMPRESA".equalsIgnoreCase(dto.getTipoDono())) {
            Empresa empresa = empresaSelecionador.selecionar(dto.getIdDono());
            if (empresa.getEndereco() != null) {
                throw new RuntimeException("Empresa já possui endereço. Use a atualização.");
            }
            empresa.setEndereco(endereco);
            empresaRepositorio.save(empresa);
            return converterParaExibirDTO(empresa.getEndereco(), empresa.getId());
        }

        throw new RuntimeException("Tipo de dono inválido. Use 'USUARIO' ou 'EMPRESA'.");
    }

    public void atualizarViaDTO(EnderecoAtualizarDTO dto) {
        Endereco endereco = selecionador.selecionar(dto.getId());

        Usuario logadoAtualizar = ContextoSeguranca.getUsuario();
        if (logadoAtualizar != null && logadoAtualizar.getPerfis().contains(PerfilUsuario.VENDEDOR)) {
            // Verifica se o endereço pertence a uma empresa
            boolean pertenceEmpresa = empresaRepositorio.findAll().stream()
                    .anyMatch(e -> e.getEndereco() != null && e.getEndereco().getId().equals(dto.getId()));
            if (pertenceEmpresa) {
                throw new AccessDeniedException("Vendedor não pode gerenciar endereços de empresas.");
            }
            // Verifica se o usuário dono é CLIENTE ou si mesmo
            Usuario donoEndereco = usuarioRepositorio.findAll().stream()
                    .filter(u -> u.getEndereco() != null && u.getEndereco().getId().equals(dto.getId()))
                    .findFirst().orElse(null);
            if (donoEndereco != null) {
                boolean ehSiMesmo = logadoAtualizar.getId().equals(donoEndereco.getId());
                boolean ehCliente = donoEndereco.getPerfis().contains(PerfilUsuario.CLIENTE);
                if (!ehSiMesmo && !ehCliente) {
                    throw new AccessDeniedException("Vendedor só pode gerenciar endereços de clientes ou de si mesmo.");
                }
            }
        }

        Endereco dadosAtualizacao = new Endereco();
        dadosAtualizacao.setEstado(dto.getEstado());
        dadosAtualizacao.setCidade(dto.getCidade());
        dadosAtualizacao.setBairro(dto.getBairro());
        dadosAtualizacao.setRua(dto.getRua());
        dadosAtualizacao.setNumero(dto.getNumero());
        dadosAtualizacao.setCodigoPostal(dto.getCodigoPostal());
        dadosAtualizacao.setInformacoesAdicionais(dto.getInformacoesAdicionais());

        atualizador.atualizar(endereco, dadosAtualizacao);
        repositorio.save(endereco);
    }

    public void excluir(Long idEndereco) {
        // 1. Tenta descobrir se o dono é um Usuário
        // Usando o método do repositório ou findByEnderecoId
        Usuario usuario = usuarioRepositorio.findAll().stream()
                .filter(u -> u.getEndereco() != null && u.getEndereco().getId().equals(idEndereco))
                .findFirst()
                .orElse(null);

        if (usuario != null) {
            usuario.setEndereco(null); // Desvincula
            usuarioRepositorio.save(usuario); // Salva o pai, o orphanRemoval deleta o filho
            return;
        }

        // 2. Se não for usuário, tenta descobrir se o dono é uma Empresa
        Empresa empresa = empresaRepositorio.findAll().stream()
                .filter(e -> e.getEndereco() != null && e.getEndereco().getId().equals(idEndereco))
                .findFirst()
                .orElse(null);

        if (empresa != null) {
            empresa.setEndereco(null); // Desvincula
            empresaRepositorio.save(empresa); // Salva a empresa, deleta o endereço
            return;
        }

        // 3. Caso o endereço exista mas não esteja vinculado a ninguém (difícil na sua regra, mas possível)
        if (repositorio.existsById(idEndereco)) {
            repositorio.deleteById(idEndereco);
            return;
        }

        throw new RuntimeException("Endereço não encontrado ou vínculo inexistente.");
    }

    private void preencherDados(Endereco endereco, EnderecoCadastrarDTO dto) {
        endereco.setEstado(dto.getEstado());
        endereco.setCidade(dto.getCidade());
        endereco.setBairro(dto.getBairro());
        endereco.setRua(dto.getRua());
        endereco.setNumero(dto.getNumero());
        endereco.setCodigoPostal(dto.getCodigoPostal());
        endereco.setInformacoesAdicionais(dto.getInformacoesAdicionais());
    }

    private EnderecoExibirDTO converterParaExibirDTO(Endereco endereco, Long donoId) {
        EnderecoExibirDTO dto = new EnderecoExibirDTO();
        dto.setId(endereco.getId());
        dto.setEstado(endereco.getEstado());
        dto.setCidade(endereco.getCidade());
        dto.setBairro(endereco.getBairro());
        dto.setRua(endereco.getRua());
        dto.setNumero(endereco.getNumero());
        dto.setCodigoPostal(endereco.getCodigoPostal());
        dto.setInformacoesAdicionais(endereco.getInformacoesAdicionais());
        dto.setIdDono(donoId);
        return dto;
    }
}