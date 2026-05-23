package com.autobots.automanager.dtos.Email;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class EmailCadastrarDTO {
    @NotNull(message = "ID do Usuario é obrigatório")
    private Long usuarioId;
    @NotBlank(message = "Endereço de email é obrigatório")
    private String endereco;
}