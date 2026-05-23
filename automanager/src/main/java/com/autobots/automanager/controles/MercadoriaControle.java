package com.autobots.automanager.controles;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import com.autobots.automanager.dtos.Mercadoria.MercadoriaAtualizarDTO;
import com.autobots.automanager.dtos.Mercadoria.MercadoriaCadastrarDTO;
import com.autobots.automanager.dtos.Mercadoria.MercadoriaExibirDTO;
import com.autobots.automanager.dtos.Venda.VendaExibirDTO;
import com.autobots.automanager.modeladores.MercadoriaModelador;
import com.autobots.automanager.modeladores.VendaModelador;
import com.autobots.automanager.servicos.MercadoriaServico;
import com.autobots.automanager.servicos.VendaServico;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/mercadorias")
public class MercadoriaControle {

    @Autowired
    private MercadoriaServico servico;

    @Autowired
    private MercadoriaModelador modelador;

    @Autowired
    private VendaServico vendaServico;

    @Autowired
    private VendaModelador vendaModelador;

    @PostMapping
    public ResponseEntity<MercadoriaExibirDTO> criar(@Valid @RequestBody MercadoriaCadastrarDTO dto) {
        MercadoriaExibirDTO mercadoriaCriada = servico.cadastrarViaDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelador.toModel(mercadoriaCriada));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<MercadoriaExibirDTO>> listar() {
        List<MercadoriaExibirDTO> mercadorias = servico.buscarTodos();
        List<MercadoriaExibirDTO> modelos = mercadorias.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(MercadoriaControle.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MercadoriaExibirDTO> obterPorId(@PathVariable Long id) {
        MercadoriaExibirDTO mercadoria = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(mercadoria));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CollectionModel<MercadoriaExibirDTO>> listarPorFornecedor(@PathVariable Long usuarioId) {
        List<MercadoriaExibirDTO> mercadorias = servico.buscarPorFornecedor(usuarioId);
        
        List<MercadoriaExibirDTO> modelos = mercadorias.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(MercadoriaControle.class).listarPorFornecedor(usuarioId)).withSelfRel(),
                linkTo(methodOn(UsuarioControle.class).obterPorId(usuarioId)).withRel("fornecedor")));
    }

    @GetMapping("/{id}/vendas")
    public ResponseEntity<CollectionModel<VendaExibirDTO>> listarVendasDaMercadoria(@PathVariable Long id) {
        // Supondo que você tenha essa lógica no VendaServico
        List<VendaExibirDTO> vendas = vendaServico.buscarPorMercadoria(id);
        
        List<VendaExibirDTO> modelos = vendas.stream()
                .map(vendaModelador::toModel)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(MercadoriaControle.class).listarVendasDaMercadoria(id)).withSelfRel(),
                linkTo(methodOn(MercadoriaControle.class).obterPorId(id)).withRel("mercadoria")));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<CollectionModel<MercadoriaExibirDTO>> listarPorEmpresa(@PathVariable Long empresaId) {
        List<MercadoriaExibirDTO> mercadorias = servico.buscarPorEmpresa(empresaId);
        
        List<MercadoriaExibirDTO> modelos = mercadorias.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(MercadoriaControle.class).listarPorEmpresa(empresaId)).withSelfRel(),
                linkTo(methodOn(EmpresaControle.class).obterPorId(empresaId)).withRel("empresa")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MercadoriaExibirDTO> atualizar(
            @PathVariable Long id, 
            @Valid @RequestBody MercadoriaAtualizarDTO dto) {
        dto.setId(id);
        servico.atualizarViaDTO(dto);
        MercadoriaExibirDTO atualizado = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}