package com.autobots.automanager.seguranca;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.servicos.AutenticacaoServico;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class FiltroSeguranca extends OncePerRequestFilter {

    @Autowired
    private AutenticacaoServico autenticacaoServico;

    private static final String[] ROTAS_PUBLICAS = {
        "/auth/login",
        "/usuarios",  // GET /usuarios é público (listar)
        "/empresas",  // GET /empresas é público (listar)
        "/mercadorias", // GET /mercadorias é público (listar)
        "/servicos",  // GET /servicos é público (listar)
        "/veiculos",  // GET /veiculos é público (listar)
        "/vendas"     // GET /vendas é público (listar)
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            String metodo = request.getMethod();

            // Limpa contexto de segurança anterior
            ContextoSeguranca.limpar();

            // Permite GETs nas rotas públicas sem token
            if (metodo.equals("GET") && isRotaPublica(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Permite POST em /usuarios (cadastro público)
            if (metodo.equals("POST") && path.equals("/usuarios")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Permite login sem token
            if (path.equals("/auth/login")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Requer token para todas as outras rotas
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String token = header.substring(7); // Remove "Bearer "
            Usuario usuario = autenticacaoServico.validarToken(token);
            ContextoSeguranca.setUsuario(usuario);

            // Bloqueia POST, PUT, DELETE para não-FUNCIONÁRIO
            if ((metodo.equals("POST") || metodo.equals("PUT") || metodo.equals("DELETE")) &&
                !usuario.getPerfis().contains(PerfilUsuario.FUNCIONARIO)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            filterChain.doFilter(request, response);
        } finally {
            ContextoSeguranca.limpar();
        }
    }

    private boolean isRotaPublica(String path) {
        for (String rota : ROTAS_PUBLICAS) {
            if (path.startsWith(rota)) {
                return true;
            }
        }
        return false;
    }
}
