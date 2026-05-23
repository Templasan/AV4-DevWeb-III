package com.autobots.automanager.dtos.Cliente;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClienteCadastrarDTO {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    @NotBlank(message = "Nome social é obrigatório")
    private String nomeSocial;
    
    @NotNull(message = "Data de nascimento é obrigatória")
    private Date dataNascimento;
}