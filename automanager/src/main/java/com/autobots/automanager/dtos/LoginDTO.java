package com.autobots.automanager.dtos;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {
    @NotBlank(message = "Nome de usuário é obrigatório")
    private String nomeUsuario;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;
}
