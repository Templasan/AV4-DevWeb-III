package com.autobots.automanager.modelo.Email;

import org.springframework.stereotype.Component;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.modelo.StringVerificadorNulo;

@Component
public class EmailAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Email email, Email atualizacao) {
        if (atualizacao != null) {
            if (!verificador.verificar(atualizacao.getEndereco())) {
                email.setEndereco(atualizacao.getEndereco());
            }
        }
    }
}