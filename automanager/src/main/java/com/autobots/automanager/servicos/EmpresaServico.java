package com.autobots.automanager.servicos;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autobots.automanager.dtos.Empresa.EmpresaAtualizarDTO;
import com.autobots.automanager.dtos.Empresa.EmpresaCadastrarDTO;
import com.autobots.automanager.dtos.Empresa.EmpresaExibirDTO;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.modelo.Empresa.EmpresaAtualizador;
import com.autobots.automanager.modelo.Empresa.EmpresaSelecionador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;

@Service
public class EmpresaServico {

    @Autowired
    private EmpresaRepositorio repositorio;

    @Autowired
    private EmpresaSelecionador selecionador;

    @Autowired
    private EmpresaAtualizador atualizador;

    public void cadastrar(Empresa empresa) {
        // Garantindo que a data de cadastro seja setada se for um novo registro
        if (empresa.getCadastro() == null) {
            empresa.setCadastro(new java.util.Date());
        }
        repositorio.save(empresa);
    }

    public List<EmpresaExibirDTO> buscarTodos() {
        List<Empresa> empresas = repositorio.findAll();
        return empresas.stream()
                .map(this::converterParaExibirDTO)
                .collect(Collectors.toList());
    }

    public EmpresaExibirDTO buscarPorIdDTO(Long id) {
        Empresa empresa = selecionador.selecionar(id);
        return converterParaExibirDTO(empresa);
    }

    public void atualizar(Empresa atualizacao) {
        Empresa empresa = selecionador.selecionar(atualizacao.getId());
        atualizador.atualizar(empresa, atualizacao);
        repositorio.save(empresa);
    }

    public void excluir(Long id) {
        Empresa empresa = selecionador.selecionar(id);
        repositorio.delete(empresa);
    }

    public EmpresaExibirDTO cadastrarViaDTO(EmpresaCadastrarDTO dto) {
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial(dto.getRazaoSocial());
        empresa.setNomeFantasia(dto.getNomeFantasia());
        
        // A data de cadastro é automática no momento do salvamento
        empresa.setCadastro(new java.util.Date());

        // Se o seu DTO de cadastro trouxer Endereço ou Telefones,
        // você deve associá-los aqui. Caso contrário, isso é feito via rotas separadas.
        
        this.cadastrar(empresa);
        return converterParaExibirDTO(empresa);
    }

    public void atualizarViaDTO(EmpresaAtualizarDTO dto) {
        Empresa empresa = selecionador.selecionar(dto.getId());
        
        Empresa dadosAtualizacao = new Empresa();
        dadosAtualizacao.setRazaoSocial(dto.getRazaoSocial());
        dadosAtualizacao.setNomeFantasia(dto.getNomeFantasia());

        atualizador.atualizar(empresa, dadosAtualizacao);
        repositorio.save(empresa);
    }

    private EmpresaExibirDTO converterParaExibirDTO(Empresa empresa) {
        EmpresaExibirDTO dto = new EmpresaExibirDTO();
        dto.setId(empresa.getId());
        dto.setRazaoSocial(empresa.getRazaoSocial());
        dto.setNomeFantasia(empresa.getNomeFantasia());
        dto.setCadastro(empresa.getCadastro());
        
        // Seta o ID do endereço para que o Modelador possa gerar o link
        if (empresa.getEndereco() != null) {
            dto.setIdEndereco(empresa.getEndereco().getId());
        }
        
        return dto;
    }
}