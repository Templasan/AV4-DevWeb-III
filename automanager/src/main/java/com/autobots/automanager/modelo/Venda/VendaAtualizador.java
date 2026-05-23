package com.autobots.automanager.modelo.Venda;

import org.springframework.stereotype.Component;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.modelo.StringVerificadorNulo;

@Component
public class VendaAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Venda venda, Venda atualizacao) {
        if (atualizacao != null) {
            if (!verificador.verificar(atualizacao.getIdentificacao())) {
                venda.setIdentificacao(atualizacao.getIdentificacao());
            }
            if (atualizacao.getCadastro() != null) {
                venda.setCadastro(atualizacao.getCadastro());
            }
        }
    }
}