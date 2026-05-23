package com.autobots.automanager.modeladores;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.*;
import com.autobots.automanager.dtos.Veiculo.VeiculoExibirDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class VeiculoModelador implements RepresentationModelAssembler<VeiculoExibirDTO, VeiculoExibirDTO> {

    @Override
    public VeiculoExibirDTO toModel(VeiculoExibirDTO dto) {
        Long id = dto.getId();
        Long idDono = dto.getProprietarioId();

        // Self e coleção
        dto.add(linkTo(methodOn(VeiculoControle.class).obterPorId(id)).withSelfRel());
        dto.add(linkTo(methodOn(VeiculoControle.class).listar()).withRel("veiculos"));

        // Ações no próprio veículo
        dto.add(linkTo(methodOn(VeiculoControle.class).atualizar(id, null)).withRel("editar"));
        dto.add(linkTo(methodOn(VeiculoControle.class).deletar(id)).withRel("excluir"));

        // Proprietário e todos os veículos dele
        if (idDono != null) {
            dto.add(linkTo(methodOn(UsuarioControle.class).obterPorId(idDono)).withRel("proprietario"));
            dto.add(linkTo(methodOn(VeiculoControle.class).listarPorProprietario(idDono)).withRel("veiculos_do_proprietario"));
        }

        // Histórico de vendas deste veículo
        dto.add(linkTo(methodOn(VendaControle.class).listarPorVeiculo(id)).withRel("historico_vendas"));

        // Ação de criar nova venda (agendamento de serviço)
        dto.add(linkTo(methodOn(VendaControle.class).criar(null)).withRel("agendar_servico"));

        return dto;
    }
}