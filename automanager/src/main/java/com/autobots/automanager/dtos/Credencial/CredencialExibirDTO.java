package com.autobots.automanager.dtos.Credencial;

import lombok.Data;
import java.util.Date;

@Data
public class CredencialExibirDTO {
    private Long id;
    private String tipo; // "USUARIO_SENHA" ou "CODIGO_BARRA"
    private String nomeUsuario;
    private Long codigo;
    private Date criacao;
    private Date ultimoAcesso;
    private boolean inativo;
}
