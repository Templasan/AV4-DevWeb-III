package com.autobots.automanager.modeladores;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.dtos.Telefone.TelefoneExibirDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class TelefoneModelador implements RepresentationModelAssembler<TelefoneExibirDTO, TelefoneExibirDTO> {

    @Override
    public TelefoneExibirDTO toModel(TelefoneExibirDTO dto) {
        Long id = dto.getId();
        Long idDono = dto.getIdDono(); // Usando idDono para abranger Usuario/Empresa

        // 1. Link para o detalhe da entidade (Self)
        dto.add(linkTo(methodOn(TelefoneControle.class).obterPorId(id)).withSelfRel());

        // 2. Link de retorno para a coleção completa
        dto.add(linkTo(methodOn(TelefoneControle.class).listar()).withRel("telefones"));

        // 3. Link para a rota de edição (PUT)
        dto.add(linkTo(methodOn(TelefoneControle.class).atualizar(id, null)).withRel("editar"));

        // 4. Link para a rota de exclusão (DELETE)
        // Nota: Ajuste os parâmetros conforme a assinatura do seu deletar (id ou id, idDono)
        dto.add(linkTo(methodOn(TelefoneControle.class).deletar(id, idDono)).withRel("excluir"));

        // Links relacionados ao dono
        if (idDono != null) {
            // 5. Link para o usuário dono (Perfil)
            dto.add(linkTo(methodOn(UsuarioControle.class).obterPorId(idDono)).withRel("dono"));

            // 6. Link para todos os telefones deste mesmo dono
            // Atende ao requisito: "Link para todos os telefones deste mesmo dono"
            dto.add(linkTo(methodOn(TelefoneControle.class).listarPorUsuario(idDono)).withRel("telefones_do_dono"));
        }

        return dto;
    }
}