package com.autobots.automanager.modeladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.*;
import com.autobots.automanager.dtos.Usuario.UsuarioExibirDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.stream.Collectors;

@Component
public class UsuarioModelador implements RepresentationModelAssembler<UsuarioExibirDTO, UsuarioExibirDTO> {

    @Autowired
    private EmailModelador emailModelador;

    @Autowired
    private EnderecoModelador enderecoModelador;

    @Override
    public UsuarioExibirDTO toModel(UsuarioExibirDTO usuario) {
        Long id = usuario.getId();

        // Self e coleção
        usuario.add(linkTo(methodOn(UsuarioControle.class).obterPorId(id)).withSelfRel());
        usuario.add(linkTo(methodOn(UsuarioControle.class).listar()).withRel("usuarios"));

        // Ações no próprio usuário
        usuario.add(linkTo(methodOn(UsuarioControle.class).atualizar(id, null)).withRel("editar"));
        usuario.add(linkTo(methodOn(UsuarioControle.class).deletar(id)).withRel("excluir"));

        // Sub-recursos do usuário
        usuario.add(linkTo(methodOn(DocumentoControle.class).listarPorUsuario(id)).withRel("documentos"));
        usuario.add(linkTo(methodOn(TelefoneControle.class).listarPorUsuario(id)).withRel("telefones"));
        usuario.add(linkTo(methodOn(EnderecoControle.class).obterPorUsuario(id)).withRel("endereco"));

        // Entidades relacionadas ao usuário por papel
        usuario.add(linkTo(methodOn(VeiculoControle.class).listarPorProprietario(id)).withRel("veiculos"));
        usuario.add(linkTo(methodOn(VendaControle.class).listarPorUsuarioComoCliente(id)).withRel("vendas-como-cliente"));
        usuario.add(linkTo(methodOn(VendaControle.class).listarPorUsuarioComoFuncionario(id)).withRel("vendas-como-funcionario"));
        usuario.add(linkTo(methodOn(MercadoriaControle.class).listarPorFornecedor(id)).withRel("mercadorias_fornecidas"));

        // Sub-modela emails com seus próprios links HATEOAS
        if (usuario.getEmails() != null) {
            usuario.setEmails(usuario.getEmails().stream()
                .map(emailModelador::toModel)
                .collect(Collectors.toSet()));
        }

        // Sub-modela endereço com seus próprios links HATEOAS
        if (usuario.getEndereco() != null) {
            usuario.setEndereco(enderecoModelador.toModel(usuario.getEndereco()));
        }

        return usuario;
    }
}