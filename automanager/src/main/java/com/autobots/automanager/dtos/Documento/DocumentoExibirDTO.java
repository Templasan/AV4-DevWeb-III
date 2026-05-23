package com.autobots.automanager.dtos.Documento;

import java.util.Date;

import org.springframework.hateoas.RepresentationModel;

import com.autobots.automanager.enumeracoes.TipoDocumento;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DocumentoExibirDTO extends RepresentationModel<DocumentoExibirDTO> {
    private Long id;
    private TipoDocumento tipo;
    private String numero;
    private Date dataEmissao;
    private Long idUsuario;
}