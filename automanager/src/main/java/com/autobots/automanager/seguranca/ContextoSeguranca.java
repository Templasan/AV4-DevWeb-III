package com.autobots.automanager.seguranca;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.autobots.automanager.adaptadores.UserDetailsImpl;
import com.autobots.automanager.entidades.Usuario;

public class ContextoSeguranca {
    private static final ThreadLocal<Usuario> usuarioAtual = new ThreadLocal<>();

    public static void setUsuario(Usuario usuario) {
        usuarioAtual.set(usuario);
    }

    /**
     * Retorna o usuário logado.
     * Primeira tentativa: ThreadLocal (populado por filtros manuais).
     * Fallback: SecurityContextHolder (populado pelo Autorizador JWT e pelo
     * Spring Security Test via @WithMockUser / @WithUserDetails).
     */
    public static Usuario getUsuario() {
        Usuario u = usuarioAtual.get();
        if (u != null) return u;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) auth.getPrincipal()).getUsuario();
        }
        return null;
    }

    public static void limpar() {
        usuarioAtual.remove();
    }
}
