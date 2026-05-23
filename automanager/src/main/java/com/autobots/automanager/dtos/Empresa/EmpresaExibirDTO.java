package com.autobots.automanager.dtos.Empresa;

import java.util.Date;
import org.springframework.hateoas.RepresentationModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class EmpresaExibirDTO extends RepresentationModel<EmpresaExibirDTO> {
    private Long id;
    private String razaoSocial;
    private String nomeFantasia;
    private Date cadastro;
    private Long idEndereco;
}