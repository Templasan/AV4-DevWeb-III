package com.autobots.automanager.controles;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import com.autobots.automanager.dtos.Venda.VendaAtualizarDTO;
import com.autobots.automanager.dtos.Venda.VendaCadastrarDTO;
import com.autobots.automanager.dtos.Venda.VendaExibirDTO;
import com.autobots.automanager.modeladores.VendaModelador;
import com.autobots.automanager.servicos.VendaServico;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/vendas")
public class VendaControle {

    @Autowired
    private VendaServico servico;

    @Autowired
    private VendaModelador modelador;

    @PostMapping
    public ResponseEntity<VendaExibirDTO> criar(@Valid @RequestBody VendaCadastrarDTO dto) {
        VendaExibirDTO vendaCriada = servico.cadastrarViaDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelador.toModel(vendaCriada));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<VendaExibirDTO>> listar() {
        List<VendaExibirDTO> vendas = servico.buscarTodos();
        List<VendaExibirDTO> modelos = vendas.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(VendaControle.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaExibirDTO> obterPorId(@PathVariable Long id) {
        VendaExibirDTO vendaDTO = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(vendaDTO));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<CollectionModel<VendaExibirDTO>> listarPorEmpresa(@PathVariable Long empresaId) {
        List<VendaExibirDTO> vendas = servico.listarPorEmpresa(empresaId);
        return converterParaCollection(vendas, "vendas-empresa");
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CollectionModel<VendaExibirDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        List<VendaExibirDTO> vendas = servico.listarPorUsuario(usuarioId);
        return converterParaCollection(vendas, "vendas-usuario");
    }

    @GetMapping("/veiculo/{veiculoId}")
    public ResponseEntity<CollectionModel<VendaExibirDTO>> listarPorVeiculo(@PathVariable Long veiculoId) {
        List<VendaExibirDTO> vendas = servico.listarPorVeiculo(veiculoId);
        return converterParaCollection(vendas, "vendas-veiculo");
    }

    // Método auxiliar para converter a lista em CollectionModel com HATEOAS
    private ResponseEntity<CollectionModel<VendaExibirDTO>> converterParaCollection(List<VendaExibirDTO> vendas, String rel) {
        List<VendaExibirDTO> modelos = vendas.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(VendaControle.class).listar()).withRel(rel)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendaExibirDTO> atualizar(
            @PathVariable Long id, 
            @Valid @RequestBody VendaAtualizarDTO dto) {
        dto.setId(id);
        servico.atualizarViaDTO(dto);
        VendaExibirDTO atualizado = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizado));
    }

    @PostMapping("/{id}/mercadorias/{mercadoriaId}")
    public ResponseEntity<VendaExibirDTO> adicionarMercadoria(
            @PathVariable Long id,
            @PathVariable Long mercadoriaId) {
        servico.adicionarMercadoria(id, mercadoriaId);
        VendaExibirDTO atualizada = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizada));
    }

    @DeleteMapping("/{id}/mercadorias/{mercadoriaId}")
    public ResponseEntity<VendaExibirDTO> removerMercadoria(
            @PathVariable Long id,
            @PathVariable Long mercadoriaId) {
        servico.removerMercadoria(id, mercadoriaId);
        VendaExibirDTO atualizada = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizada));
    }

    @PostMapping("/{id}/servicos/{servicoId}")
    public ResponseEntity<VendaExibirDTO> adicionarServico(
            @PathVariable Long id,
            @PathVariable Long servicoId) {
        servico.adicionarServico(id, servicoId);
        VendaExibirDTO atualizada = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizada));
    }

    @DeleteMapping("/{id}/servicos/{servicoId}")
    public ResponseEntity<VendaExibirDTO> removerServico(
            @PathVariable Long id,
            @PathVariable Long servicoId) {
        servico.removerServico(id, servicoId);
        VendaExibirDTO atualizada = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizada));
    }

    @PostMapping("/{id}/veiculo/{veiculoId}")
    public ResponseEntity<VendaExibirDTO> adicionarVeiculo(
            @PathVariable Long id,
            @PathVariable Long veiculoId) {
        servico.adicionarVeiculo(id, veiculoId);
        VendaExibirDTO atualizada = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizada));
    }

    @DeleteMapping("/{id}/veiculo")
    public ResponseEntity<VendaExibirDTO> removerVeiculo(@PathVariable Long id) {
        servico.removerVeiculo(id);
        VendaExibirDTO atualizada = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}