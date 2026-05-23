package com.autobots.automanager.dtos.Cliente;

import java.util.Date;
import java.util.List;
import org.springframework.hateoas.RepresentationModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.autobots.automanager.dtos.Endereco.EnderecoExibirDTO;
import com.autobots.automanager.dtos.Documento.DocumentoExibirDTO;
import com.autobots.automanager.dtos.Telefone.TelefoneExibirDTO;

@Data
@EqualsAndHashCode(callSuper = false)
public class ClienteExibirDTO extends RepresentationModel<ClienteExibirDTO> {
    private Long id;
    private String nome;
    private String nomeSocial;
    private Date dataNascimento;
    private Date dataCadastro;

    private EnderecoExibirDTO endereco;
    private List<TelefoneExibirDTO> telefones;
    private List<DocumentoExibirDTO> documentos;
}