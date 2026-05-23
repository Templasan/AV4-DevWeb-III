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
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    public ResponseEntity<VendaExibirDTO> criar(@Valid @RequestBody VendaCadastrarDTO dto) {
        VendaExibirDTO vendaCriada = servico.cadastrarViaDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelador.toModel(vendaCriada));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<CollectionModel<VendaExibirDTO>> listar() {
        List<VendaExibirDTO> vendas = servico.buscarTodos();
        List<VendaExibirDTO> modelos = vendas.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(modelos, 
                linkTo(methodOn(VendaControle.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isParticipanteVenda(#id)")
    public ResponseEntity<VendaExibirDTO> obterPorId(@PathVariable Long id) {
        VendaExibirDTO vendaDTO = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(vendaDTO));
    }

    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<CollectionModel<VendaExibirDTO>> listarPorEmpresa(@PathVariable Long empresaId) {
        List<VendaExibirDTO> vendas = servico.listarPorEmpresa(empresaId);
        return converterParaCollection(vendas, "vendas-empresa");
    }

    @GetMapping("/usuario/{usuarioId}/cliente")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isProprioUsuario(#usuarioId)")
    public ResponseEntity<CollectionModel<VendaExibirDTO>> listarPorUsuarioComoCliente(@PathVariable Long usuarioId) {
        List<VendaExibirDTO> vendas = servico.listarPorUsuarioComoCliente(usuarioId);
        return converterParaCollection(vendas, "vendas-como-cliente");
    }

    @GetMapping("/usuario/{usuarioId}/funcionario")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isProprioUsuario(#usuarioId)")
    public ResponseEntity<CollectionModel<VendaExibirDTO>> listarPorUsuarioComoFuncionario(@PathVariable Long usuarioId) {
        List<VendaExibirDTO> vendas = servico.listarPorUsuarioComoFuncionario(usuarioId);
        return converterParaCollection(vendas, "vendas-como-funcionario");
    }

    @GetMapping("/veiculo/{veiculoId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public ResponseEntity<CollectionModel<VendaExibirDTO>> listarPorVeiculo(@PathVariable Long veiculoId) {
        List<VendaExibirDTO> vendas = servico.listarPorVeiculo(veiculoId);
        return converterParaCollection(vendas, "vendas-veiculo");
    }

    // Método auxiliar para converter a lista em CollectionModel com HATEOAS
    private ResponseEntity<CollectionModel<VendaExibirDTO>> converterParaCollection(List<VendaExibirDTO> vendas, String rel) {
        List<VendaExibirDTO> modelos = vendas.stream()
                .map(modelador::toModel)
                .collect(Collectors.toList());

        CollectionModel<VendaExibirDTO> collection = CollectionModel.of(modelos,
                linkTo(methodOn(VendaControle.class).listar()).withRel(rel));
        // Adicionar link self
        collection.add(linkTo(methodOn(VendaControle.class).listar()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isVendedorDaVenda(#id)")
    public ResponseEntity<VendaExibirDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody VendaAtualizarDTO dto) {
        dto.setId(id);
        servico.atualizarViaDTO(dto);
        VendaExibirDTO atualizado = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizado));
    }

    @PostMapping("/{id}/mercadorias/{mercadoriaId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isVendedorDaVenda(#id)")
    public ResponseEntity<VendaExibirDTO> adicionarMercadoria(
            @PathVariable Long id,
            @PathVariable Long mercadoriaId) {
        servico.adicionarMercadoria(id, mercadoriaId);
        VendaExibirDTO atualizada = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizada));
    }

    @DeleteMapping("/{id}/mercadorias/{mercadoriaId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isVendedorDaVenda(#id)")
    public ResponseEntity<VendaExibirDTO> removerMercadoria(
            @PathVariable Long id,
            @PathVariable Long mercadoriaId) {
        servico.removerMercadoria(id, mercadoriaId);
        VendaExibirDTO atualizada = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizada));
    }

    @PostMapping("/{id}/servicos/{servicoId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isVendedorDaVenda(#id)")
    public ResponseEntity<VendaExibirDTO> adicionarServico(
            @PathVariable Long id,
            @PathVariable Long servicoId) {
        servico.adicionarServico(id, servicoId);
        VendaExibirDTO atualizada = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizada));
    }

    @DeleteMapping("/{id}/servicos/{servicoId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isVendedorDaVenda(#id)")
    public ResponseEntity<VendaExibirDTO> removerServico(
            @PathVariable Long id,
            @PathVariable Long servicoId) {
        servico.removerServico(id, servicoId);
        VendaExibirDTO atualizada = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizada));
    }

    @PostMapping("/{id}/veiculo/{veiculoId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isVendedorDaVenda(#id)")
    public ResponseEntity<VendaExibirDTO> adicionarVeiculo(
            @PathVariable Long id,
            @PathVariable Long veiculoId) {
        servico.adicionarVeiculo(id, veiculoId);
        VendaExibirDTO atualizada = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizada));
    }

    @DeleteMapping("/{id}/veiculo")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isVendedorDaVenda(#id)")
    public ResponseEntity<VendaExibirDTO> removerVeiculo(@PathVariable Long id) {
        servico.removerVeiculo(id);
        VendaExibirDTO atualizada = servico.buscarPorIdDTO(id);
        return ResponseEntity.ok(modelador.toModel(atualizada));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE') || @segurancaUtil.isVendedorDaVenda(#id)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}