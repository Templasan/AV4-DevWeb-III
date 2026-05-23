package com.autobots.automanager.dtos.Telefone;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class TelefoneAtualizarDTO {
    private Long id;
    
    @NotBlank(message = "DDD é obrigatório")
    @Size(min = 2, max = 2, message = "DDD deve ter 2 dígitos")
    private String ddd;
    
    @NotBlank(message = "Número é obrigatório")
    @Size(min = 8, max = 9, message = "Número deve ter entre 8 e 9 dígitos")
    private String numero;
}
