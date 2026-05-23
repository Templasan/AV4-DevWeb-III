package com.autobots.automanager.servicos;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autobots.automanager.dtos.Veiculo.VeiculoAtualizarDTO;
import com.autobots.automanager.dtos.Veiculo.VeiculoCadastrarDTO;
import com.autobots.automanager.dtos.Veiculo.VeiculoExibirDTO;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.Veiculo.VeiculoAtualizador;
import com.autobots.automanager.modelo.Veiculo.VeiculoSelecionador;
import com.autobots.automanager.modelo.Usuario.UsuarioSelecionador;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VeiculoRepositorio;

@Service
public class VeiculoServico {

    @Autowired
    private VeiculoRepositorio repositorio;

    @Autowired
    private VeiculoSelecionador selecionador;

    @Autowired
    private VeiculoAtualizador atualizador;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private UsuarioSelecionador usuarioSelecionador;

    public void cadastrar(Veiculo veiculo) {
        repositorio.save(veiculo);
    }

    public List<VeiculoExibirDTO> buscarTodos() {
        List<Veiculo> veiculos = repositorio.findAll();
        return veiculos.stream()
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    public List<VeiculoExibirDTO> buscarPorProprietario(Long usuarioId) {
        // 1. Busca o usuário (proprietário)
        Usuario proprietario = usuarioSelecionador.selecionar(usuarioId);
        
        // 2. Retorna a lista de veículos dele convertida para DTO
        // Supondo que na sua entidade Usuario exista o método getVeiculos()
        return proprietario.getVeiculos().stream()
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    public VeiculoExibirDTO buscarPorIdDTO(Long id) {
        Veiculo veiculo = selecionador.selecionar(id);
        return converterParaExibirDTO(veiculo);
    }

    public void atualizar(Veiculo atualizacao) {
        Veiculo veiculo = selecionador.selecionar(atualizacao.getId());
        atualizador.atualizar(veiculo, atualizacao);
        repositorio.save(veiculo);
    }

    public void excluir(Long id) {
        Veiculo veiculo = selecionador.selecionar(id);
        repositorio.delete(veiculo);
    }

    public VeiculoExibirDTO cadastrarViaDTO(VeiculoCadastrarDTO dto) {
        Veiculo veiculo = new Veiculo();
        veiculo.setTipo(dto.getTipo());
        veiculo.setModelo(dto.getModelo());
        veiculo.setPlaca(dto.getPlaca());

        // Vinculação do proprietário via ID fornecido no DTO
        if (dto.getProprietarioId() != null) {
            Usuario proprietario = usuarioSelecionador.selecionar(dto.getProprietarioId());
            veiculo.setProprietario(proprietario);
        }

        this.cadastrar(veiculo);
        return converterParaExibirDTO(veiculo);
    }

    public void atualizarViaDTO(VeiculoAtualizarDTO dto) {
        Veiculo veiculo = selecionador.selecionar(dto.getId());
        
        Veiculo dadosAtualizacao = new Veiculo();
        dadosAtualizacao.setTipo(dto.getTipo());
        dadosAtualizacao.setModelo(dto.getModelo());
        dadosAtualizacao.setPlaca(dto.getPlaca());

        atualizador.atualizar(veiculo, dadosAtualizacao);
        repositorio.save(veiculo);
    }

    private VeiculoExibirDTO converterParaExibirDTO(Veiculo veiculo) {
    VeiculoExibirDTO dto = new VeiculoExibirDTO();
    dto.setId(veiculo.getId());
    dto.setTipo(veiculo.getTipo());
    dto.setModelo(veiculo.getModelo());
    dto.setPlaca(veiculo.getPlaca());
    
    // Identifica o Proprietário (Usuario)
    // No seu modelo, o Veiculo geralmente tem uma relação direta com Usuario
    if (veiculo.getProprietario() != null) {
        dto.setProprietarioId(veiculo.getProprietario().getId());
        dto.setNomeProprietario(veiculo.getProprietario().getNome());
    } else {
        // Lógica de segurança: caso o vínculo não seja direto na entidade Veiculo,
        // mas sim o Usuario que tenha a lista de Veiculos (mapeamento bidirecional)
        usuarioRepositorio.findAll().stream()
            .filter(u -> u.getVeiculos().contains(veiculo))
            .findFirst()
            .ifPresent(u -> {
                dto.setProprietarioId(u.getId());
                dto.setNomeProprietario(u.getNome());
            });
    }
    
    return dto;
}
}