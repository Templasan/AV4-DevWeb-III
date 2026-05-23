package com.autobots.automanager.servicos;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dtos.Mercadoria.MercadoriaExibirDTO;
import com.autobots.automanager.dtos.Venda.VendaAtualizarDTO;
import com.autobots.automanager.dtos.Venda.VendaCadastrarDTO;
import com.autobots.automanager.dtos.Venda.VendaExibirDTO;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.modelo.Venda.VendaAtualizador;
import com.autobots.automanager.modelo.Venda.VendaSelecionador;
import com.autobots.automanager.modelo.Usuario.UsuarioSelecionador;
import com.autobots.automanager.modelo.Mercadoria.MercadoriaSelecionador;
import com.autobots.automanager.modelo.Servico.ServicoSelecionador;
import com.autobots.automanager.modelo.Empresa.EmpresaSelecionador;
import com.autobots.automanager.modelo.Veiculo.VeiculoSelecionador;
import com.autobots.automanager.repositorios.VendaRepositorio;

@Service
@Transactional
public class VendaServico {

    @Autowired
    private VendaRepositorio repositorio;

    @Autowired
    private VendaSelecionador selecionador;

    @Autowired
    private VendaAtualizador atualizador;

    @Autowired
    private UsuarioSelecionador usuarioSelecionador;

    @Autowired
    private MercadoriaSelecionador mercadoriaSelecionador;
    
    @Autowired
    private EmpresaSelecionador empresaSelecionador;
    
    @Autowired
    private VeiculoSelecionador veiculoSelecionador;

    @Autowired
    private ServicoSelecionador servicoSelecionador;

    public List<VendaExibirDTO> buscarTodos() {
        return repositorio.findAll().stream()
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    public VendaExibirDTO buscarPorIdDTO(Long id) {
        Venda venda = selecionador.selecionar(id);
        return converterParaExibirDTO(venda);
    }

    // Filtro para MercadoriaControle
    public List<VendaExibirDTO> buscarPorMercadoria(Long mercadoriaId) {
        Mercadoria mercadoria = mercadoriaSelecionador.selecionar(mercadoriaId);
        return repositorio.findAll().stream()
                .filter(v -> v.getMercadorias().contains(mercadoria))
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    // Filtro para EmpresaControle
    public List<VendaExibirDTO> listarPorEmpresa(Long empresaId) {
        Empresa empresa = empresaSelecionador.selecionar(empresaId);
        return empresa.getVendas().stream()
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    // Vendas onde o usuário foi o COMPRADOR (cliente)
    public List<VendaExibirDTO> listarPorUsuarioComoCliente(Long usuarioId) {
        Usuario usuario = usuarioSelecionador.selecionar(usuarioId);
        return repositorio.findAll().stream()
                .filter(v -> v.getCliente() != null && v.getCliente().equals(usuario))
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    // Vendas onde o usuário foi o VENDEDOR (funcionário)
    public List<VendaExibirDTO> listarPorUsuarioComoFuncionario(Long usuarioId) {
        Usuario usuario = usuarioSelecionador.selecionar(usuarioId);
        return repositorio.findAll().stream()
                .filter(v -> v.getFuncionario() != null && v.getFuncionario().equals(usuario))
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }
    
    // Filtro para VeiculoControle
    public List<VendaExibirDTO> listarPorVeiculo(Long veiculoId) {
        Veiculo veiculo = veiculoSelecionador.selecionar(veiculoId);
        return repositorio.findAll().stream()
                .filter(v -> v.getVeiculo() != null && v.getVeiculo().equals(veiculo))
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

   

    public List<VendaExibirDTO> buscarPorServico(Long servicoId) {
        // 1. Seleciona o serviço para garantir que existe
        Servico servicoEntidade = servicoSelecionador.selecionar(servicoId);
        
        // 2. Filtra todas as vendas onde a lista de serviços contém este serviço
        return repositorio.findAll().stream()
                .filter(venda -> venda.getServicos().contains(servicoEntidade))
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }


    public VendaExibirDTO cadastrarViaDTO(VendaCadastrarDTO dto) {
        Venda venda = new Venda();
        venda.setCadastro(new java.util.Date());
        venda.setIdentificacao(dto.getIdentificacao());

        // Vinculação de Atores
        venda.setCliente(usuarioSelecionador.selecionar(dto.getClienteId()));
        venda.setFuncionario(usuarioSelecionador.selecionar(dto.getFuncionarioId()));

        if (dto.getVeiculoId() != null) {
            venda.setVeiculo(veiculoSelecionador.selecionar(dto.getVeiculoId()));
        }

        // Vinculação de Mercadorias
        if (dto.getMercadoriasIds() != null && !dto.getMercadoriasIds().isEmpty()) {
            dto.getMercadoriasIds().forEach(mercadoriaId -> {
                Mercadoria mercadoria = mercadoriaSelecionador.selecionar(mercadoriaId);
                venda.getMercadorias().add(mercadoria);
            });
        }

        // Vinculação de Serviços
        if (dto.getServicosIds() != null && !dto.getServicosIds().isEmpty()) {
            dto.getServicosIds().forEach(servicoId -> {
                Servico servico = servicoSelecionador.selecionar(servicoId);
                venda.getServicos().add(servico);
            });
        }

        // Persistência
        Venda vendaSalva = repositorio.save(venda);
        return converterParaExibirDTO(vendaSalva);
    }

    public void atualizarViaDTO(VendaAtualizarDTO dto) {
        Venda venda = selecionador.selecionar(dto.getId());
        Venda dadosAtualizacao = new Venda();
        dadosAtualizacao.setIdentificacao(dto.getIdentificacao());
        
        // O atualizador cuida da mesclagem dos campos
        atualizador.atualizar(venda, dadosAtualizacao);
        repositorio.save(venda);
    }

    public void adicionarMercadoria(Long vendaId, Long mercadoriaId) {
        Venda venda = selecionador.selecionar(vendaId);
        Mercadoria mercadoria = mercadoriaSelecionador.selecionar(mercadoriaId);
        venda.getMercadorias().add(mercadoria);
        repositorio.save(venda);
    }

    public void removerMercadoria(Long vendaId, Long mercadoriaId) {
        Venda venda = selecionador.selecionar(vendaId);
        Mercadoria mercadoria = mercadoriaSelecionador.selecionar(mercadoriaId);
        venda.getMercadorias().remove(mercadoria);
        repositorio.save(venda);
    }

    public void adicionarServico(Long vendaId, Long servicoId) {
        Venda venda = selecionador.selecionar(vendaId);
        Servico servico = servicoSelecionador.selecionar(servicoId);
        venda.getServicos().add(servico);
        repositorio.save(venda);
    }

    public void removerServico(Long vendaId, Long servicoId) {
        Venda venda = selecionador.selecionar(vendaId);
        Servico servico = servicoSelecionador.selecionar(servicoId);
        venda.getServicos().remove(servico);
        repositorio.save(venda);
    }

    public void adicionarVeiculo(Long vendaId, Long veiculoId) {
        Venda venda = selecionador.selecionar(vendaId);
        Veiculo veiculo = veiculoSelecionador.selecionar(veiculoId);
        venda.setVeiculo(veiculo);
        repositorio.save(venda);
    }

    public void removerVeiculo(Long vendaId) {
        Venda venda = selecionador.selecionar(vendaId);
        venda.setVeiculo(null);
        repositorio.save(venda);
    }

    public void excluir(Long id) {
        Venda venda = selecionador.selecionar(id);
        repositorio.delete(venda);
    }

    private VendaExibirDTO converterParaExibirDTO(Venda v) {
        VendaExibirDTO dto = new VendaExibirDTO();
        dto.setId(v.getId());
        dto.setCadastro(v.getCadastro());
        dto.setIdentificacao(v.getIdentificacao());

        if (v.getCliente() != null) {
            dto.setClienteId(v.getCliente().getId());
            dto.setNomeCliente(v.getCliente().getNome());
        }
        if (v.getFuncionario() != null) {
            dto.setFuncionarioId(v.getFuncionario().getId());
            dto.setNomeFuncionario(v.getFuncionario().getNome());
        }
        if (v.getVeiculo() != null) {
            dto.setVeiculoId(v.getVeiculo().getId());
            dto.setPlacaVeiculo(v.getVeiculo().getPlaca());
        }

        if (v.getMercadorias() != null && !v.getMercadorias().isEmpty()) {
            dto.setMercadoriasIds(v.getMercadorias().stream()
                    .map(Mercadoria::getId)
                    .collect(Collectors.toSet()));
            // Embute os objetos completos para que CLIENTE veja os dados sem acessar /mercadorias
            dto.setMercadorias(v.getMercadorias().stream()
                    .map(m -> {
                        MercadoriaExibirDTO md = new MercadoriaExibirDTO();
                        md.setId(m.getId());
                        md.setNome(m.getNome());
                        md.setDescricao(m.getDescricao());
                        md.setQuantidade(m.getQuantidade());
                        md.setValor(m.getValor());
                        md.setValidade(m.getValidade());
                        md.setFabricacao(m.getFabricacao());
                        md.setCadastro(m.getCadastro());
                        return md;
                    })
                    .collect(Collectors.toList()));
        }

        if (v.getServicos() != null && !v.getServicos().isEmpty()) {
            dto.setServicosIds(v.getServicos().stream()
                    .map(Servico::getId)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }
}