package com.autobots.automanager.dtos.Servico;

import org.springframework.hateoas.RepresentationModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ServicoExibirDTO extends RepresentationModel<ServicoExibirDTO> {
    private Long id;
    private String nome;
    private double valor;
    private String descricao;
    private Long idEmpresa;
}