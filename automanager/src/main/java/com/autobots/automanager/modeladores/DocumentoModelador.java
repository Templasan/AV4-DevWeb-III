package com.autobots.automanager.modeladores;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.DocumentoControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.dtos.Documento.DocumentoExibirDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class DocumentoModelador implements RepresentationModelAssembler<DocumentoExibirDTO, DocumentoExibirDTO> {

    @Override
    public DocumentoExibirDTO toModel(DocumentoExibirDTO dto) {
        Long id = dto.getId();
        Long idDono = dto.getIdUsuario(); // Referenciando o dono do documento

        // 1. Link para o detalhe da própria entidade (Self)
        dto.add(linkTo(methodOn(DocumentoControle.class).obterPorId(id)).withSelfRel());

        // 2. Link de retorno para a coleção completa
        dto.add(linkTo(methodOn(DocumentoControle.class).listar()).withRel("documentos"));

        // 3. Link para a rota de edição (PUT)
        dto.add(linkTo(methodOn(DocumentoControle.class).atualizar(id, null)).withRel("editar"));

        // 4. Link para a rota de exclusão (DELETE)
        dto.add(linkTo(methodOn(DocumentoControle.class).deletar(id)).withRel("excluir"));

        // Links relacionados ao dono (Usuário)
        if (idDono != null) {
            // 5. Link para o usuário dono (detalhes do perfil)
            dto.add(linkTo(methodOn(UsuarioControle.class).obterPorId(idDono)).withRel("dono"));

            // 6. Link para todos os documentos deste mesmo dono (Filtro)
            // Supondo que você tenha o endpoint listarPorDono no DocumentoControle
            dto.add(linkTo(methodOn(DocumentoControle.class).listarPorUsuario(idDono)).withRel("documentos_do_dono"));
        }

        return dto;
    }
}