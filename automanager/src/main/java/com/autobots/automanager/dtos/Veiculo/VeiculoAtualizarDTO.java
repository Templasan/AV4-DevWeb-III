package com.autobots.automanager.dtos.Veiculo;

import com.autobots.automanager.enumeracoes.TipoVeiculo;
import lombok.Data;

@Data
public class VeiculoAtualizarDTO {
    private Long id;
    private TipoVeiculo tipo;
    private String modelo;
    private String placa;
}