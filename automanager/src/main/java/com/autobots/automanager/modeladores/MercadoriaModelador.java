package com.autobots.automanager.modeladores;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.controles.MercadoriaControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.dtos.Mercadoria.MercadoriaExibirDTO;

@Component
public class MercadoriaModelador implements RepresentationModelAssembler<MercadoriaExibirDTO, MercadoriaExibirDTO> {

    @Override
    public MercadoriaExibirDTO toModel(MercadoriaExibirDTO dto) {
        Long id = dto.getId();

        // Self e coleção
        dto.add(linkTo(methodOn(MercadoriaControle.class).obterPorId(id)).withSelfRel());
        dto.add(linkTo(methodOn(MercadoriaControle.class).listar()).withRel("mercadorias"));

        // Ações na própria mercadoria
        dto.add(linkTo(methodOn(MercadoriaControle.class).atualizar(id, null)).withRel("editar"));
        dto.add(linkTo(methodOn(MercadoriaControle.class).deletar(id)).withRel("excluir"));

        // Vendas que incluem esta mercadoria
        dto.add(linkTo(methodOn(MercadoriaControle.class).listarVendasDaMercadoria(id)).withRel("vendas"));

        // Empresa detentora
        if (dto.getIdEmpresa() != null) {
            dto.add(linkTo(methodOn(EmpresaControle.class).obterPorId(dto.getIdEmpresa())).withRel("empresa_detentora"));
            dto.add(linkTo(methodOn(MercadoriaControle.class).listarPorEmpresa(dto.getIdEmpresa())).withRel("mercadorias_da_empresa"));
        }

        // Fornecedor e outras mercadorias do mesmo fornecedor
        if (dto.getIdFornecedor() != null) {
            dto.add(linkTo(methodOn(UsuarioControle.class).obterPorId(dto.getIdFornecedor())).withRel("fornecedor"));
            dto.add(linkTo(methodOn(MercadoriaControle.class).listarPorFornecedor(dto.getIdFornecedor())).withRel("mercadorias_do_fornecedor"));
        }

        return dto;
    }
}