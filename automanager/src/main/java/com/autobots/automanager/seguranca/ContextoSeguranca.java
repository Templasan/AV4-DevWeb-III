package com.autobots.automanager.seguranca;

import com.autobots.automanager.entidades.Usuario;

public class ContextoSeguranca {
    private static final ThreadLocal<Usuario> usuarioAtual = new ThreadLocal<>();

    public static void setUsuario(Usuario usuario) {
        usuarioAtual.set(usuario);
    }

    public static Usuario getUsuario() {
        return usuarioAtual.get();
    }

    public static void limpar() {
        usuarioAtual.remove();
    }
}
