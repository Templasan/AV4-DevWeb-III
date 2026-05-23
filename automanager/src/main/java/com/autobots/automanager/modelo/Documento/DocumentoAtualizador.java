package com.autobots.automanager.modelo.Documento;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.modelo.StringVerificadorNulo;

@Component
public class DocumentoAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Documento documento, Documento atualizacao) {
        if (atualizacao != null) {
            if (atualizacao.getTipo() != null) {
                documento.setTipo(atualizacao.getTipo());
            }
            if (!verificador.verificar(atualizacao.getNumero())) {
                documento.setNumero(atualizacao.getNumero());
            }
            if (atualizacao.getDataEmissao() != null) {
                documento.setDataEmissao(atualizacao.getDataEmissao());
            }
        }
    }

    public void atualizar(List<Documento> documentos, List<Documento> atualizacoes) {
        if (atualizacoes == null || atualizacoes.isEmpty()) {
            return;
        }

        for (Documento atualizacao : atualizacoes) {
            for (Documento documento : documentos) {
                if (atualizacao.getId() != null && documento.getId() != null) {
                    if (atualizacao.getId().equals(documento.getId())) {
                        atualizar(documento, atualizacao);
                    }
                }
            }
        }
    }
}