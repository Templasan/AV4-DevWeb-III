package com.autobots.automanager.dtos.Telefone;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class TelefoneCadastrarDTO {
    @NotNull(message = "ID do dono é obrigatório")
    private Long idDono;
    
    @NotBlank(message = "DDD é obrigatório")
    @Size(min = 2, max = 2, message = "DDD deve ter 2 dígitos")
    private String ddd;
    
    @NotBlank(message = "Número é obrigatório")
    @Size(min = 8, max = 9, message = "Número deve ter entre 8 e 9 dígitos")
    private String numero;

    @NotBlank(message = "O tipo do dono (USUARIO ou EMPRESA) é obrigatório.")
    @Pattern(regexp = "^(USUARIO|EMPRESA)$", message = "O tipo do dono deve ser 'USUARIO' ou 'EMPRESA'.")
    private String tipoDono;
}
