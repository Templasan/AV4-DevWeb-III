package com.autobots.automanager.dtos.Email;

import org.springframework.hateoas.RepresentationModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class EmailExibirDTO extends RepresentationModel<EmailExibirDTO> {
    private Long id;
    private Long usuarioId;
    private String endereco;
}