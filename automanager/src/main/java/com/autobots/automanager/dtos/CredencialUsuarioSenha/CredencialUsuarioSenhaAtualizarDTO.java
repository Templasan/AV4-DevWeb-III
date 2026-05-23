package com.autobots.automanager.dtos.CredencialUsuarioSenha;

import lombok.Data;

@Data
public class CredencialUsuarioSenhaAtualizarDTO {
    private Long id;
    private String senha; // Geralmente só atualizamos a senha
    private boolean inativo;
}