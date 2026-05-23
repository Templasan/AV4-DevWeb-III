package com.autobots.automanager.dtos.Mercadoria;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class MercadoriaAtualizarDTO {

    @NotNull(message = "O ID da mercadoria é obrigatório para realizar a atualização.")
    private Long id;

    private Long idFornecedor;
    @NotBlank(message = "O nome da mercadoria não pode estar em branco.")
    private String nome;
    private Date validade;
    private Date fabricacao;
    @PositiveOrZero(message = "A quantidade não pode ser negativa.")
    private Long quantidade;
    @Positive(message = "O valor deve ser maior que zero.")
    private Double valor;
    private String descricao;
}