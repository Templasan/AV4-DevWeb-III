package com.autobots.automanager.controles;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import com.autobots.automanager.dtos.Email.EmailAtualizarDTO;
import com.autobots.automanager.dtos.Email.EmailCadastrarDTO;
import com.autobots.automanager.dtos.Email.EmailExibirDTO;
import com.autobots.automanager.modeladores.EmailModelador;
import com.autobots.automanager.servicos.EmailServico;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/emails")
public class EmailControle {

    @Autowired
    private EmailServico servico;

    @Autowired
    private EmailModelador modelador;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<EmailExibirDTO> criar(@Valid @RequestBody EmailCadastrarDTO dto) {
        EmailExibirDTO criado = servico.cadastrarViaDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelador.toModel(criado));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<CollectionModel<EmailExibirDTO>> listar() {
        List<EmailExibirDTO> emails = servico.buscarTodos();
        List<EmailExibirDTO> modelos = emails.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(EmailControle.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<EmailExibirDTO> obterPorId(@PathVariable Long id) {
        EmailExibirDTO email = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(email));
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isProprioUsuario(#usuarioId)")
    public ResponseEntity<CollectionModel<EmailExibirDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        List<EmailExibirDTO> emails = servico.buscarPorUsuario(usuarioId);
        
        List<EmailExibirDTO> modelos = emails.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(EmailControle.class).listarPorUsuario(usuarioId)).withSelfRel(),
                linkTo(methodOn(UsuarioControle.class).obterPorId(usuarioId)).withRel("usuario")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<EmailExibirDTO> atualizar(@PathVariable Long id, @Valid @RequestBody EmailAtualizarDTO dto) {
        dto.setId(id);
        servico.atualizarViaDTO(dto);
        EmailExibirDTO atualizado = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}