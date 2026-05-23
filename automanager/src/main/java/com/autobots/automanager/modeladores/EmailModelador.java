package com.autobots.automanager.modeladores;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmailControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.dtos.Email.EmailExibirDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class EmailModelador implements RepresentationModelAssembler<EmailExibirDTO, EmailExibirDTO> {

    @Override
    public EmailExibirDTO toModel(EmailExibirDTO dto) {
        Long id = dto.getId();
        // Certifique-se que o DTO tenha esse campo preenchido pelo serviço
        Long idDono = dto.getUsuarioId(); 

        // 1. Link para o detalhe da entidade (Self)
        dto.add(linkTo(methodOn(EmailControle.class).obterPorId(id)).withSelfRel());

        // 2. Link de retorno para a coleção completa
        dto.add(linkTo(methodOn(EmailControle.class).listar()).withRel("emails"));

        // 3. Link para a rota de edição (PUT)
        dto.add(linkTo(methodOn(EmailControle.class).atualizar(id, null)).withRel("editar"));

        // 4. Link para a rota de exclusão (DELETE)
        dto.add(linkTo(methodOn(EmailControle.class).deletar(id)).withRel("excluir"));

        // 5. Link para o usuário dono
        if (idDono != null) {
            dto.add(linkTo(methodOn(UsuarioControle.class).obterPorId(idDono)).withRel("dono"));
        }

        return dto;
    }
}