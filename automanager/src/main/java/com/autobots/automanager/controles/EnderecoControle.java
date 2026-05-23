package com.autobots.automanager.controles;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import com.autobots.automanager.dtos.Endereco.EnderecoAtualizarDTO;
import com.autobots.automanager.dtos.Endereco.EnderecoCadastrarDTO;
import com.autobots.automanager.dtos.Endereco.EnderecoExibirDTO;
import com.autobots.automanager.modeladores.EnderecoModelador;
import com.autobots.automanager.servicos.EnderecoServico;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/enderecos")
public class EnderecoControle {

    @Autowired
    private EnderecoServico servico;

    @Autowired
    private EnderecoModelador modelador;

    @PostMapping
    public ResponseEntity<EnderecoExibirDTO> criar(@Valid @RequestBody EnderecoCadastrarDTO dto) {
        EnderecoExibirDTO enderecoCriado = servico.cadastrarViaDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelador.toModel(enderecoCriado));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EnderecoExibirDTO>> listar() {
        List<EnderecoExibirDTO> enderecos = servico.buscarTodos();
        List<EnderecoExibirDTO> modelos = enderecos.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(EnderecoControle.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnderecoExibirDTO> obterPorId(@PathVariable Long id) {
        EnderecoExibirDTO endereco = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(endereco));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<EnderecoExibirDTO> obterPorUsuario(@PathVariable Long usuarioId) {
        EnderecoExibirDTO endereco = servico.buscarPorUsuario(usuarioId);
        return ResponseEntity.ok(modelador.toModel(endereco));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<EnderecoExibirDTO> obterPorEmpresa(@PathVariable Long empresaId) {
        EnderecoExibirDTO endereco = servico.buscarPorEmpresa(empresaId);
        return ResponseEntity.ok(modelador.toModel(endereco));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnderecoExibirDTO> atualizar(
            @PathVariable Long id, 
            @Valid @RequestBody EnderecoAtualizarDTO dto) {
        dto.setId(id);
        servico.atualizarViaDTO(dto);
        EnderecoExibirDTO atualizado = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}