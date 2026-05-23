package com.autobots.automanager.dtos.Documento;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.autobots.automanager.enumeracoes.TipoDocumento;

import lombok.Data;

@Data
public class DocumentoAtualizarDTO {
    @NotNull(message = "ID é obrigatório")
    private Long id;

    private TipoDocumento tipo;

    @NotBlank(message = "Número do documento é obrigatório")
    private String numero;

    private Date dataEmissao;
}
