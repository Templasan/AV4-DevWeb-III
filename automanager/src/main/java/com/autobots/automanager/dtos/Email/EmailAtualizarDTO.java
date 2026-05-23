package com.autobots.automanager.dtos.Email;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class EmailAtualizarDTO {
    @NotNull(message = "ID é obrigatório")
    private Long id;
    @NotBlank(message = "Endereço de email é obrigatório")
    private String endereco;
}