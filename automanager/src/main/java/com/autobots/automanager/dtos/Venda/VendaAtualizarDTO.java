package com.autobots.automanager.dtos.Venda;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VendaAtualizarDTO {
    private Long id;
    @NotBlank(message = "A identificação da venda é obrigatória.")
    private String identificacao;
    private Date cadastro;
}