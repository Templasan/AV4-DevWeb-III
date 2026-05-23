package com.autobots.automanager.modeladores;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.dtos.Endereco.EnderecoExibirDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class EnderecoModelador implements RepresentationModelAssembler<EnderecoExibirDTO, EnderecoExibirDTO> {

    @Override
    public EnderecoExibirDTO toModel(EnderecoExibirDTO dto) {
        Long id = dto.getId();
        Long idDono = dto.getIdDono();

        // 1. Link para o detalhe (Self)
        dto.add(linkTo(methodOn(EnderecoControle.class).obterPorId(id)).withSelfRel());

        // 2. Link para a coleção completa
        dto.add(linkTo(methodOn(EnderecoControle.class).listar()).withRel("enderecos"));

        // 3. Link para edição (PUT)
        dto.add(linkTo(methodOn(EnderecoControle.class).atualizar(id, null)).withRel("editar"));

        // 4. Link para exclusão (DELETE)
        dto.add(linkTo(methodOn(EnderecoControle.class).deletar(id)).withRel("excluir"));

        // 5. Link para o usuário dono
        if (idDono != null) {
            dto.add(linkTo(methodOn(UsuarioControle.class).obterPorId(idDono)).withRel("dono"));
        }

        return dto;
    }
}