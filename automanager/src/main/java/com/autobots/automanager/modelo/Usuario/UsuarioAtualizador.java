package com.autobots.automanager.modelo.Usuario;

import org.springframework.stereotype.Component;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.StringVerificadorNulo;

@Component
public class UsuarioAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Usuario usuario, Usuario atualizacao) {
        if (atualizacao != null) {
            if (!verificador.verificar(atualizacao.getNome())) {
                usuario.setNome(atualizacao.getNome());
            }
            if (!verificador.verificar(atualizacao.getNomeSocial())) {
                usuario.setNomeSocial(atualizacao.getNomeSocial());
            }
        }
    }
}