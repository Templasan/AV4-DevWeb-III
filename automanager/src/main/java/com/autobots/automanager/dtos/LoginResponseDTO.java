package com.autobots.automanager.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.autobots.automanager.dtos.Usuario.UsuarioExibirDTO;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private UsuarioExibirDTO usuario;
    private String token;
}
