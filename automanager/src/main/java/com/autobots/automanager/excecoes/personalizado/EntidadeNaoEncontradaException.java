package com.autobots.automanager.excecoes.personalizado;

public class EntidadeNaoEncontradaException extends RuntimeException {
    
    public EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }

    public EntidadeNaoEncontradaException(String titulo, String mensagem) {
        super(mensagem);
    }
}