package com.autobots.automanager.dtos.Credencial;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class AtualizarSenhaDTO {
    @NotBlank(message = "Nova senha é obrigatória")
    private String novaSenha;
}
