package com.autobots.automanager.dtos.Veiculo;

import com.autobots.automanager.enumeracoes.TipoVeiculo;
import lombok.Data;

@Data
public class VeiculoCadastrarDTO {
    private TipoVeiculo tipo;
    private String modelo;
    private String placa;
    private Long proprietarioId; // Passamos apenas o ID para o serviço buscar e vincular
}