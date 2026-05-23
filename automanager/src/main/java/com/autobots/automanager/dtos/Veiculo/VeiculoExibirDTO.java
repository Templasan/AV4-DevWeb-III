package com.autobots.automanager.dtos.Veiculo;

import org.springframework.hateoas.RepresentationModel;
import com.autobots.automanager.enumeracoes.TipoVeiculo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class VeiculoExibirDTO extends RepresentationModel<VeiculoExibirDTO> {
    private Long id;
    private TipoVeiculo tipo;
    private String modelo;
    private String placa;
    private String nomeProprietario;
    private Long proprietarioId;
}                                                                   