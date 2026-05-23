package com.autobots.automanager.dtos.Documento;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.autobots.automanager.enumeracoes.TipoDocumento;

import lombok.Data;

@Data
public class DocumentoCadastrarDTO {
    @NotNull(message = "ID do Usuario é obrigatório")
    private Long usuarioId;

    @NotNull(message = "Tipo de documento é obrigatório")
    private TipoDocumento tipo;

    @NotBlank(message = "Número do documento é obrigatório")
    private String numero;

    @NotNull(message = "Data de emissão é obrigatória")
    private Date dataEmissao;
}
