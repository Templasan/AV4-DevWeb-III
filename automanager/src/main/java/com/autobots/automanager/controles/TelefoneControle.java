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

import com.autobots.automanager.dtos.Telefone.TelefoneAtualizarDTO;
import com.autobots.automanager.dtos.Telefone.TelefoneCadastrarDTO;
import com.autobots.automanager.dtos.Telefone.TelefoneExibirDTO;
import com.autobots.automanager.modeladores.TelefoneModelador;
import com.autobots.automanager.servicos.TelefoneServico;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/telefones")
public class TelefoneControle {

    @Autowired
    private TelefoneServico servico;

    @Autowired
    private TelefoneModelador modelador;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<TelefoneExibirDTO> criar(@Valid @RequestBody TelefoneCadastrarDTO dto) {
        TelefoneExibirDTO telefoneCriado = servico.cadastrarViaDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelador.toModel(telefoneCriado));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<CollectionModel<TelefoneExibirDTO>> listar() {
        List<TelefoneExibirDTO> telefones = servico.buscarTodos();
        List<TelefoneExibirDTO> modelos = telefones.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(TelefoneControle.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<TelefoneExibirDTO> obterPorId(@PathVariable Long id) {
        TelefoneExibirDTO telefone = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(telefone));
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isProprioUsuario(#usuarioId)")
    public ResponseEntity<CollectionModel<TelefoneExibirDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        List<TelefoneExibirDTO> telefones = servico.buscarPorUsuario(usuarioId);
        
        List<TelefoneExibirDTO> modelos = telefones.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(TelefoneControle.class).listarPorUsuario(usuarioId)).withSelfRel(),
                linkTo(methodOn(UsuarioControle.class).obterPorId(usuarioId)).withRel("usuario")));
    }

    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<CollectionModel<TelefoneExibirDTO>> listarPorEmpresa(@PathVariable Long empresaId) {
        List<TelefoneExibirDTO> telefones = servico.buscarPorEmpresa(empresaId);
        
        List<TelefoneExibirDTO> modelos = telefones.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(TelefoneControle.class).listarPorEmpresa(empresaId)).withSelfRel(),
                linkTo(methodOn(EmpresaControle.class).obterPorId(empresaId)).withRel("empresa")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<TelefoneExibirDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TelefoneAtualizarDTO dto) {
        dto.setId(id);
        servico.atualizarViaDTO(dto);
        TelefoneExibirDTO atualizado = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizado));
    }

    @DeleteMapping("/{id}/{idDono}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<Void> deletar(@PathVariable Long id, @PathVariable Long idDono) {
        servico.excluir(id, idDono);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}