package com.autobots.automanager.controles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import com.autobots.automanager.dtos.Credencial.CredencialInputDTO;
import com.autobots.automanager.dtos.Credencial.CredencialExibirDTO;
import com.autobots.automanager.dtos.Credencial.AtualizarSenhaDTO;
import com.autobots.automanager.servicos.CredencialServico;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/usuarios/{usuarioId}/credenciais")
public class CredencialControle {

    @Autowired
    private CredencialServico credencialServico;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CredencialExibirDTO> adicionarCredencial(
            @PathVariable Long usuarioId,
            @Valid @RequestBody CredencialInputDTO dto) {
        try {
            CredencialExibirDTO credencial = credencialServico.adicionarCredencial(usuarioId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(credencial);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<CredencialExibirDTO>> listarCredenciais(
            @PathVariable Long usuarioId) {
        List<CredencialExibirDTO> credenciais = credencialServico.listarCredenciaisDoUsuario(usuarioId);
        return ResponseEntity.ok(CollectionModel.of(credenciais,
                linkTo(methodOn(CredencialControle.class).listarCredenciais(usuarioId)).withSelfRel()));
    }

    @PutMapping("/{credencialId}/senha")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CredencialExibirDTO> atualizarSenha(
            @PathVariable Long usuarioId,
            @PathVariable Long credencialId,
            @Valid @RequestBody AtualizarSenhaDTO dto) {
        try {
            CredencialExibirDTO credencial = credencialServico.atualizarCredencialUsuarioSenha(credencialId, dto.getNovaSenha());
            return ResponseEntity.ok(credencial);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{credencialId}/desativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desativarCredencial(
            @PathVariable Long usuarioId,
            @PathVariable Long credencialId) {
        try {
            credencialServico.desativarCredencial(credencialId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{credencialId}/ativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> ativarCredencial(
            @PathVariable Long usuarioId,
            @PathVariable Long credencialId) {
        try {
            credencialServico.ativarCredencial(credencialId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{credencialId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removerCredencial(
            @PathVariable Long usuarioId,
            @PathVariable Long credencialId) {
        try {
            credencialServico.removerCredencial(usuarioId, credencialId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
