package com.autobots.automanager.dtos.Endereco;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class EnderecoCadastrarDTO {
    
    @NotNull(message = "ID do dono é obrigatório.")
    private Long idDono;

    @NotBlank(message = "O tipoDono (USUARIO ou EMPRESA) é obrigatório.")
    @Pattern(regexp = "^(USUARIO|EMPRESA)$", message = "O tipo do dono deve ser 'USUARIO' ou 'EMPRESA'.")
    private String tipoDono;
    
    @NotBlank(message = "Estado é obrigatório.")
    @Size(min = 2, max = 2, message = "Estado deve ter exatamente 2 caracteres (Ex: SP).")
    private String estado;
    
    @NotBlank(message = "Cidade é obrigatória.")
    private String cidade;
    
    @NotBlank(message = "Bairro é obrigatório.")
    private String bairro;
    
    @NotBlank(message = "Rua é obrigatória.")
    private String rua;
    
    @NotBlank(message = "Número é obrigatório.")
    private String numero;
    
    @NotBlank(message = "Código postal é obrigatório.")
    private String codigoPostal;
    
    private String informacoesAdicionais;
}