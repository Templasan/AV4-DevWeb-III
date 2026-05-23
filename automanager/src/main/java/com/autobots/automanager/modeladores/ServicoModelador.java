package com.autobots.automanager.modeladores;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.controles.ServicoControle;
import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.dtos.Servico.ServicoExibirDTO;

@Component
public class ServicoModelador implements RepresentationModelAssembler<ServicoExibirDTO, ServicoExibirDTO> {

    @Override
    public ServicoExibirDTO toModel(ServicoExibirDTO dto) {
        Long id = dto.getId();

        // Self e coleção
        dto.add(linkTo(methodOn(ServicoControle.class).obterPorId(id)).withSelfRel());
        dto.add(linkTo(methodOn(ServicoControle.class).listar()).withRel("servicos"));

        // Ações no próprio serviço
        dto.add(linkTo(methodOn(ServicoControle.class).atualizar(id, null)).withRel("editar"));
        dto.add(linkTo(methodOn(ServicoControle.class).deletar(id)).withRel("excluir"));

        // Vendas que incluem este serviço
        dto.add(linkTo(methodOn(ServicoControle.class).listarVendasDoServico(id)).withRel("vendas"));

        // Ação de criar uma nova venda usando este serviço
        dto.add(linkTo(methodOn(VendaControle.class).criar(null)).withRel("criar_venda_com_servico"));

        // Empresa vinculada ao serviço
        if (dto.getIdEmpresa() != null) {
            dto.add(linkTo(methodOn(EmpresaControle.class).obterPorId(dto.getIdEmpresa())).withRel("empresa_detentora"));
        }

        return dto;
    }
}