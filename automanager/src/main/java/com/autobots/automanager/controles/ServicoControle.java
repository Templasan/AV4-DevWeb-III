package com.autobots.automanager.controles;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import com.autobots.automanager.dtos.Servico.ServicoAtualizarDTO;
import com.autobots.automanager.dtos.Servico.ServicoCadastrarDTO;
import com.autobots.automanager.dtos.Servico.ServicoExibirDTO;
import com.autobots.automanager.dtos.Venda.VendaExibirDTO;
import com.autobots.automanager.modeladores.ServicoModelador;
import com.autobots.automanager.modeladores.VendaModelador;
import com.autobots.automanager.servicos.ServicoServico;
import com.autobots.automanager.servicos.VendaServico;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/servicos")
public class ServicoControle {

    @Autowired
    private ServicoServico servico;

    @Autowired
    private ServicoModelador modelador;

    @Autowired
    private VendaServico vendaServico;

    @Autowired
    private VendaModelador vendaModelador;

    @PostMapping
    public ResponseEntity<ServicoExibirDTO> criar(@Valid @RequestBody ServicoCadastrarDTO dto) {
        ServicoExibirDTO servicoCriado = servico.cadastrarViaDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelador.toModel(servicoCriado));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<ServicoExibirDTO>> listar() {
        List<ServicoExibirDTO> servicos = servico.buscarTodos();
        List<ServicoExibirDTO> modelos = servicos.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(ServicoControle.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoExibirDTO> obterPorId(@PathVariable Long id) {
        ServicoExibirDTO servicoDTO = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(servicoDTO));
    }

    @GetMapping("/{id}/vendas")
    public ResponseEntity<CollectionModel<VendaExibirDTO>> listarVendasDoServico(@PathVariable Long id) {
        // Busca as vendas que possuem este serviço através do VendaServico
        List<VendaExibirDTO> vendas = vendaServico.buscarPorServico(id);
        
        List<VendaExibirDTO> modelos = vendas.stream()
                .map(vendaModelador::toModel)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(ServicoControle.class).listarVendasDoServico(id)).withSelfRel(),
                linkTo(methodOn(ServicoControle.class).obterPorId(id)).withRel("servico")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoExibirDTO> atualizar(
            @PathVariable Long id, 
            @Valid @RequestBody ServicoAtualizarDTO dto) {
        dto.setId(id);
        servico.atualizarViaDTO(dto);
        ServicoExibirDTO atualizado = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}