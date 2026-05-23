package com.autobots.automanager.dtos.Usuario;

import java.util.Set;
import org.springframework.hateoas.RepresentationModel;

import com.autobots.automanager.dtos.Email.EmailExibirDTO;
import com.autobots.automanager.dtos.Endereco.EnderecoExibirDTO;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UsuarioExibirDTO extends RepresentationModel<UsuarioExibirDTO> {
    private Long id;
    private String nome;
    private String nomeSocial;
    private Set<PerfilUsuario> perfis;
    private Set<EmailExibirDTO> emails;
    private EnderecoExibirDTO endereco;
}