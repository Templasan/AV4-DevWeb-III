package com.autobots.automanager.dtos.CredencialCodigoBarra;

import java.sql.Date;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;

@Data
public class CredencialCodigoBarraExibirDTO extends RepresentationModel<CredencialCodigoBarraExibirDTO> {
    private Long id;
    private long codigo;
    private Date criacao;
    private Date ultimoAcesso;
    private boolean inativo;
}