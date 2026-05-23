package com.autobots.automanager.servicos;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dtos.Servico.ServicoAtualizarDTO;
import com.autobots.automanager.dtos.Servico.ServicoCadastrarDTO;
import com.autobots.automanager.dtos.Servico.ServicoExibirDTO;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.modelo.Servico.ServicoAtualizador;
import com.autobots.automanager.modelo.Servico.ServicoSelecionador;
import com.autobots.automanager.modelo.Empresa.EmpresaSelecionador;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import com.autobots.automanager.repositorios.EmpresaRepositorio;

@Service
@Transactional
public class ServicoServico {

    @Autowired
    private ServicoRepositorio repositorio;

    @Autowired
    private ServicoSelecionador selecionador;

    @Autowired
    private ServicoAtualizador atualizador;

    @Autowired
    private EmpresaSelecionador empresaSelecionador;

    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    public List<ServicoExibirDTO> buscarTodos() {
        return repositorio.findAll().stream()
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    public ServicoExibirDTO buscarPorIdDTO(Long id) {
        Servico servico = selecionador.selecionar(id);
        return converterParaExibirDTO(servico);
    }

    // Filtro para EmpresaControle
    public List<ServicoExibirDTO> buscarPorEmpresa(Long empresaId) {
        Empresa empresa = empresaSelecionador.selecionar(empresaId);
        return empresa.getServicos().stream()
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    public ServicoExibirDTO cadastrarViaDTO(ServicoCadastrarDTO dto) {
        Servico servico = new Servico();
        servico.setNome(dto.getNome());
        servico.setValor(dto.getValor());
        servico.setDescricao(dto.getDescricao());
        
        // Salva o serviço primeiro
        repositorio.save(servico);

        // Vincula à Empresa
        if (dto.getIdEmpresa() != null) {
            Empresa empresa = empresaSelecionador.selecionar(dto.getIdEmpresa());
            empresa.getServicos().add(servico);
            empresaRepositorio.save(empresa);
        }

        return converterParaExibirDTO(servico);
    }

    public void atualizarViaDTO(ServicoAtualizarDTO dto) {
        Servico servico = selecionador.selecionar(dto.getId());
        Servico dadosAtualizacao = new Servico();
        dadosAtualizacao.setNome(dto.getNome());
        dadosAtualizacao.setValor(dto.getValor());
        dadosAtualizacao.setDescricao(dto.getDescricao());

        atualizador.atualizar(servico, dadosAtualizacao);
        repositorio.save(servico);
    }

    public void excluir(Long id) {
        Servico servico = selecionador.selecionar(id);
        
        // Remove vínculos em Empresas para evitar erro de integridade
        empresaRepositorio.findAll().stream()
            .filter(e -> e.getServicos().contains(servico))
            .forEach(e -> {
                e.getServicos().remove(servico);
                empresaRepositorio.save(e);
            });

        repositorio.delete(servico);
    }

    private ServicoExibirDTO converterParaExibirDTO(Servico servico) {
        ServicoExibirDTO dto = new ServicoExibirDTO();
        dto.setId(servico.getId());
        dto.setNome(servico.getNome());
        dto.setValor(servico.getValor());
        dto.setDescricao(servico.getDescricao());
        
        // Descobre qual empresa é dona deste serviço para o ID no DTO (necessário para HATEOAS)
        empresaRepositorio.findAll().stream()
            .filter(e -> e.getServicos().contains(servico))
            .findFirst()
            .ifPresent(e -> dto.setIdEmpresa(e.getId()));
            
        return dto;
    }
}