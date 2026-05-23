package com.autobots.automanager.dtos.Venda;

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.hateoas.RepresentationModel;
import com.autobots.automanager.dtos.Mercadoria.MercadoriaExibirDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class VendaExibirDTO extends RepresentationModel<VendaExibirDTO> {
    private Long id;
    private String identificacao;
    private Date cadastro;

    // IDs necessários para o HATEOAS (geração de links)
    private Long clienteId;
    private Long funcionarioId;
    private Long veiculoId;
    private Set<Long> mercadoriasIds;
    private Set<Long> servicosIds;

    // Campos auxiliares para exibição direta no JSON
    private String nomeCliente;
    private String nomeFuncionario;
    private String placaVeiculo;

    // Dados de mercadorias embutidos — permite que CLIENTE veja os detalhes sem
    // precisar acessar a rota /mercadorias (restrita a ADMIN/GERENTE/VENDEDOR)
    private List<MercadoriaExibirDTO> mercadorias;
}