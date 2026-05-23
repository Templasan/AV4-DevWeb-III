package com.autobots.automanager.dtos.Credencial;

import lombok.Data;

@Data
public class CredencialInputDTO {
    private String tipo; // "USUARIO_SENHA" ou "CODIGO_BARRA"
    private String nomeUsuario;
    private String senha;
    private Long codigo;
}