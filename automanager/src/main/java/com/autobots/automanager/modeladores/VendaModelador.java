package com.autobots.automanager.modeladores;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.*;
import com.autobots.automanager.dtos.Venda.VendaExibirDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class VendaModelador implements RepresentationModelAssembler<VendaExibirDTO, VendaExibirDTO> {

    @Override
    public VendaExibirDTO toModel(VendaExibirDTO dto) {
        Long id = dto.getId();

        // Self e coleção
        dto.add(linkTo(methodOn(VendaControle.class).obterPorId(id)).withSelfRel());
        dto.add(linkTo(methodOn(VendaControle.class).listar()).withRel("vendas"));

        // Ações na própria venda
        dto.add(linkTo(methodOn(VendaControle.class).atualizar(id, null)).withRel("editar"));
        dto.add(linkTo(methodOn(VendaControle.class).deletar(id)).withRel("excluir"));

        // Navegação para cliente e funcionário
        if (dto.getClienteId() != null) {
            dto.add(linkTo(methodOn(UsuarioControle.class).obterPorId(dto.getClienteId())).withRel("cliente"));
            dto.add(linkTo(methodOn(VendaControle.class).listarPorUsuarioComoCliente(dto.getClienteId())).withRel("vendas_do_cliente"));
        }
        if (dto.getFuncionarioId() != null) {
            dto.add(linkTo(methodOn(UsuarioControle.class).obterPorId(dto.getFuncionarioId())).withRel("funcionario"));
            dto.add(linkTo(methodOn(VendaControle.class).listarPorUsuarioComoFuncionario(dto.getFuncionarioId())).withRel("vendas_do_funcionario"));
        }

        // Navegação para o veículo
        if (dto.getVeiculoId() != null) {
            dto.add(linkTo(methodOn(VeiculoControle.class).obterPorId(dto.getVeiculoId())).withRel("veiculo"));
            dto.add(linkTo(methodOn(VendaControle.class).listarPorVeiculo(dto.getVeiculoId())).withRel("vendas_do_veiculo"));
            dto.add(linkTo(methodOn(VendaControle.class).removerVeiculo(id)).withRel("remover_veiculo"));
        }

        // Ação para adicionar veículo
        dto.add(linkTo(methodOn(VendaControle.class).adicionarVeiculo(id, null)).withRel("adicionar_veiculo"));

        // Link individual para cada mercadoria
        if (dto.getMercadoriasIds() != null) {
            for (Long mercadoriaId : dto.getMercadoriasIds()) {
                dto.add(linkTo(methodOn(MercadoriaControle.class).obterPorId(mercadoriaId))
                        .withRel("mercadoria_" + mercadoriaId));
                dto.add(linkTo(methodOn(VendaControle.class).removerMercadoria(id, mercadoriaId))
                        .withRel("remover_mercadoria_" + mercadoriaId));
            }
        }

        // Link individual para cada serviço
        if (dto.getServicosIds() != null) {
            for (Long servicoId : dto.getServicosIds()) {
                dto.add(linkTo(methodOn(ServicoControle.class).obterPorId(servicoId))
                        .withRel("servico_" + servicoId));
                dto.add(linkTo(methodOn(VendaControle.class).removerServico(id, servicoId))
                        .withRel("remover_servico_" + servicoId));
            }
        }

        // Ações de gerenciamento de itens
        dto.add(linkTo(methodOn(VendaControle.class).adicionarMercadoria(id, null)).withRel("adicionar_mercadoria"));
        dto.add(linkTo(methodOn(VendaControle.class).adicionarServico(id, null)).withRel("adicionar_servico"));

        return dto;
    }
}
