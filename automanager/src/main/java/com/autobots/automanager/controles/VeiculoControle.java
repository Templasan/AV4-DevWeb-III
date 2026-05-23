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

import com.autobots.automanager.dtos.Veiculo.VeiculoAtualizarDTO;
import com.autobots.automanager.dtos.Veiculo.VeiculoCadastrarDTO;
import com.autobots.automanager.dtos.Veiculo.VeiculoExibirDTO;
import com.autobots.automanager.modeladores.VeiculoModelador;
import com.autobots.automanager.servicos.VeiculoServico;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/veiculos")
public class VeiculoControle {

    @Autowired
    private VeiculoServico servico;

    @Autowired
    private VeiculoModelador modelador;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<VeiculoExibirDTO> criar(@Valid @RequestBody VeiculoCadastrarDTO dto) {
        VeiculoExibirDTO veiculoCriado = servico.cadastrarViaDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelador.toModel(veiculoCriado));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<CollectionModel<VeiculoExibirDTO>> listar() {
        List<VeiculoExibirDTO> veiculos = servico.buscarTodos();
        List<VeiculoExibirDTO> modelos = veiculos.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(VeiculoControle.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<VeiculoExibirDTO> obterPorId(@PathVariable Long id) {
        VeiculoExibirDTO veiculoDTO = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(veiculoDTO));
    }

    @GetMapping("/proprietario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isProprioUsuario(#usuarioId)")
    public ResponseEntity<CollectionModel<VeiculoExibirDTO>> listarPorProprietario(@PathVariable Long usuarioId) {
        List<VeiculoExibirDTO> veiculos = servico.buscarPorProprietario(usuarioId);
        
        List<VeiculoExibirDTO> modelos = veiculos.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(VeiculoControle.class).listarPorProprietario(usuarioId)).withSelfRel(),
                linkTo(methodOn(UsuarioControle.class).obterPorId(usuarioId)).withRel("proprietario")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<VeiculoExibirDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody VeiculoAtualizarDTO dto) {
        dto.setId(id);
        servico.atualizarViaDTO(dto);
        VeiculoExibirDTO atualizado = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}