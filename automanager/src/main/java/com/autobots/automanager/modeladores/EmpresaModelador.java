package com.autobots.automanager.modeladores;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.*;
import com.autobots.automanager.dtos.Empresa.EmpresaExibirDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class EmpresaModelador implements RepresentationModelAssembler<EmpresaExibirDTO, EmpresaExibirDTO> {

    @Override
    public EmpresaExibirDTO toModel(EmpresaExibirDTO dto) {
        Long id = dto.getId();

        // 1. Links Básicos (Self e Coleção)
        dto.add(linkTo(methodOn(EmpresaControle.class).obterPorId(id)).withSelfRel());
        dto.add(linkTo(methodOn(EmpresaControle.class).listar()).withRel("empresas"));

        // 2. Links de Ação da Própria Entidade (PUT/DELETE)
        dto.add(linkTo(methodOn(EmpresaControle.class).atualizar(id, null)).withRel("editar"));
        dto.add(linkTo(methodOn(EmpresaControle.class).deletar(id)).withRel("excluir"));

        // Sub-recursos da empresa
        dto.add(linkTo(methodOn(EnderecoControle.class).obterPorEmpresa(id)).withRel("endereco"));
        dto.add(linkTo(methodOn(TelefoneControle.class).listarPorEmpresa(id)).withRel("telefones"));
        dto.add(linkTo(methodOn(MercadoriaControle.class).listarPorEmpresa(id)).withRel("mercadorias"));
        dto.add(linkTo(methodOn(VendaControle.class).listarPorEmpresa(id)).withRel("vendas"));

        // Usuários filtrados por perfil
        dto.add(linkTo(methodOn(UsuarioControle.class).listarClientes(id)).withRel("clientes"));
        dto.add(linkTo(methodOn(UsuarioControle.class).listarFuncionarios(id)).withRel("funcionarios"));
        dto.add(linkTo(methodOn(UsuarioControle.class).listarFornecedores(id)).withRel("fornecedores"));

        // Ações de criação vinculadas à empresa
        dto.add(linkTo(methodOn(VendaControle.class).criar(null)).withRel("registrar_venda"));
        dto.add(linkTo(methodOn(UsuarioControle.class).criar(null)).withRel("cadastrar_usuario"));

        return dto;
    }
}