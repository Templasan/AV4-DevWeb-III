package com.autobots.automanager.controles;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import com.autobots.automanager.dtos.Documento.DocumentoAtualizarDTO;
import com.autobots.automanager.dtos.Documento.DocumentoCadastrarDTO;
import com.autobots.automanager.dtos.Documento.DocumentoExibirDTO;
import com.autobots.automanager.modeladores.DocumentoModelador;
import com.autobots.automanager.servicos.DocumentoServico;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/documentos")
public class DocumentoControle {

    @Autowired
    private DocumentoServico servico;

    @Autowired
    private DocumentoModelador modelador;

    @PostMapping
    public ResponseEntity<DocumentoExibirDTO> criar(@Valid @RequestBody DocumentoCadastrarDTO dto) {
        DocumentoExibirDTO documentoCriado = servico.cadastrarViaDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelador.toModel(documentoCriado));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<DocumentoExibirDTO>> listar() {
        List<DocumentoExibirDTO> documentos = servico.buscarTodos();
        List<DocumentoExibirDTO> modelos = documentos.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(DocumentoControle.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoExibirDTO> obterPorId(@PathVariable Long id) {
        DocumentoExibirDTO documento = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(documento));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CollectionModel<DocumentoExibirDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        List<DocumentoExibirDTO> documentos = servico.buscarPorUsuario(usuarioId);
        
        List<DocumentoExibirDTO> modelos = documentos.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());
        
        // No HATEOAS, além do self, linkamos o perfil do dono para facilitar a navegação
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(DocumentoControle.class).listarPorUsuario(usuarioId)).withSelfRel(),
                linkTo(methodOn(UsuarioControle.class).obterPorId(usuarioId)).withRel("usuario")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentoExibirDTO> atualizar(
            @PathVariable Long id, 
            @Valid @RequestBody DocumentoAtualizarDTO dto) {
        dto.setId(id);
        servico.atualizarViaDTO(dto);
        DocumentoExibirDTO atualizado = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}