package com.autobots.automanager.dtos.Mercadoria;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import lombok.Data;

@Data
public class MercadoriaCadastrarDTO {
    
    @NotNull(message = "O ID do fornecedor (usuário) é obrigatório.")
    private Long idFornecedor;

    @NotBlank(message = "O nome da mercadoria não pode estar em branco.")
    private String nome;

    @NotNull(message = "A data de validade é obrigatória.")
    private Date validade;

    @NotNull(message = "A data de fabricação é obrigatória.")
    private Date fabricacao;

    @PositiveOrZero(message = "A quantidade não pode ser negativa.")
    private long quantidade;

    @Positive(message = "O valor deve ser maior que zero.")
    private double valor;

    private String descricao;
}