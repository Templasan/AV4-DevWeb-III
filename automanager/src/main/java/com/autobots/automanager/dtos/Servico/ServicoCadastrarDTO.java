package com.autobots.automanager.dtos.Servico;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import lombok.Data;

@Data
public class ServicoCadastrarDTO {
    @NotBlank(message = "O nome do serviço é obrigatório.")
    private String nome;
    @Positive(message = "O valor do serviço deve ser maior que zero.")
    private double valor;
    private String descricao;
    private Long idEmpresa;
}