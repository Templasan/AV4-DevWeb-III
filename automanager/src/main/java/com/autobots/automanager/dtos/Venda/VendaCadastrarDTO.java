package com.autobots.automanager.dtos.Venda;

import java.util.Date;
import java.util.Set;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class VendaCadastrarDTO {
    @NotBlank(message = "Identificação não pode estar em branco")
    private String identificacao;
    private Date cadastro;
    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;
    @NotNull(message = "Funcionário é obrigatório")
    private Long funcionarioId;
    private Long veiculoId;
    private Set<Long> mercadoriasIds;
    private Set<Long> servicosIds;
}