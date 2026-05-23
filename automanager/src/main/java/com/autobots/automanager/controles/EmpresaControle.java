package com.autobots.automanager.controles;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import com.autobots.automanager.dtos.Empresa.EmpresaAtualizarDTO;
import com.autobots.automanager.dtos.Empresa.EmpresaCadastrarDTO;
import com.autobots.automanager.dtos.Empresa.EmpresaExibirDTO;
import com.autobots.automanager.modeladores.EmpresaModelador;
import com.autobots.automanager.servicos.EmpresaServico;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/empresas")
public class EmpresaControle {

    @Autowired
    private EmpresaServico servico;

    @Autowired
    private EmpresaModelador modelador;

    @PostMapping
    public ResponseEntity<EmpresaExibirDTO> criar(@Valid @RequestBody EmpresaCadastrarDTO dto) {
        EmpresaExibirDTO empresaCriada = servico.cadastrarViaDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelador.toModel(empresaCriada));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EmpresaExibirDTO>> listar() {
        List<EmpresaExibirDTO> empresas = servico.buscarTodos();
        List<EmpresaExibirDTO> modelos = empresas.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(EmpresaControle.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaExibirDTO> obterPorId(@PathVariable Long id) {
        EmpresaExibirDTO empresa = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(empresa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaExibirDTO> atualizar(
            @PathVariable Long id, 
            @Valid @RequestBody EmpresaAtualizarDTO dto) {
        dto.setId(id);
        servico.atualizarViaDTO(dto);
        EmpresaExibirDTO atualizado = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}