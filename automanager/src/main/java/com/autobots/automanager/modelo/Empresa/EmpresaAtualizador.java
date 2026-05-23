package com.autobots.automanager.modelo.Empresa;

import org.springframework.stereotype.Component;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.modelo.StringVerificadorNulo;

@Component
public class EmpresaAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Empresa empresa, Empresa atualizacao) {
        if (atualizacao != null) {
            if (!verificador.verificar(atualizacao.getRazaoSocial())) {
                empresa.setRazaoSocial(atualizacao.getRazaoSocial());
            }
            if (!verificador.verificar(atualizacao.getNomeFantasia())) {
                empresa.setNomeFantasia(atualizacao.getNomeFantasia());
            }
        }
    }
}