package com.autobots.automanager.dtos.CredencialCodigoBarra;

import lombok.Data;

@Data
public class CredencialCodigoBarraAtualizarDTO {
    private Long id;
    private long codigo;
    private boolean inativo;
}