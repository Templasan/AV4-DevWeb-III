package com.autobots.automanager.dtos.Mercadoria;

import java.util.Date;
import org.springframework.hateoas.RepresentationModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MercadoriaExibirDTO extends RepresentationModel<MercadoriaExibirDTO> {
    private Long id;
    private String nome;
    private String descricao;
    private long quantidade;
    private double valor;
    private Date validade;
    private Date fabricacao;
    private Date cadastro;
    private Long idFornecedor;
    private Long idEmpresa;
}