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

import com.autobots.automanager.dtos.Usuario.UsuarioAtualizarDTO;
import com.autobots.automanager.dtos.Usuario.UsuarioCadastrarDTO;
import com.autobots.automanager.dtos.Usuario.UsuarioExibirDTO;
import com.autobots.automanager.modeladores.UsuarioModelador;
import com.autobots.automanager.servicos.UsuarioServico;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioControle {

    @Autowired
    private UsuarioServico servico;

    @Autowired
    private UsuarioModelador modelador;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<UsuarioExibirDTO> criar(@Valid @RequestBody UsuarioCadastrarDTO dto) {
        UsuarioExibirDTO usuarioCriado = servico.cadastrarViaDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelador.toModel(usuarioCriado));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<CollectionModel<UsuarioExibirDTO>> listar() {
        List<UsuarioExibirDTO> usuarios = servico.buscarTodos();
        List<UsuarioExibirDTO> modelos = usuarios.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(UsuarioControle.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR') || @segurancaUtil.isProprioUsuario(#id)")
    public ResponseEntity<UsuarioExibirDTO> obterPorId(@PathVariable Long id) {
        UsuarioExibirDTO usuarioDTO = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(usuarioDTO));
    }

    @GetMapping("/empresa/{empresaId}/clientes")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<CollectionModel<UsuarioExibirDTO>> listarClientes(@PathVariable Long empresaId) {
        List<UsuarioExibirDTO> clientes = servico.buscarPorEmpresaETipo(empresaId, "CLIENTE");
        List<UsuarioExibirDTO> modelos = clientes.stream().map(modelador::toModel).collect(Collectors.toList());
        
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(UsuarioControle.class).listarClientes(empresaId)).withSelfRel()));
    }

    @GetMapping("/empresa/{empresaId}/funcionarios")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<CollectionModel<UsuarioExibirDTO>> listarFuncionarios(@PathVariable Long empresaId) {
        List<UsuarioExibirDTO> funcionarios = servico.buscarPorEmpresaETipo(empresaId, "FUNCIONARIO");
        List<UsuarioExibirDTO> modelos = funcionarios.stream().map(modelador::toModel).collect(Collectors.toList());
        
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(UsuarioControle.class).listarFuncionarios(empresaId)).withSelfRel()));
    }

    @GetMapping("/empresa/{empresaId}/fornecedores")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<CollectionModel<UsuarioExibirDTO>> listarFornecedores(@PathVariable Long empresaId) {
        List<UsuarioExibirDTO> fornecedores = servico.buscarPorEmpresaETipo(empresaId, "FORNECEDOR");
        List<UsuarioExibirDTO> modelos = fornecedores.stream().map(modelador::toModel).collect(Collectors.toList());
        
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(UsuarioControle.class).listarFornecedores(empresaId)).withSelfRel()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<UsuarioExibirDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAtualizarDTO dto) {
        dto.setId(id);
        servico.atualizarViaDTO(dto);
        UsuarioExibirDTO atualizado = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}