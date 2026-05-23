package com.autobots.automanager.seguranca;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.jwt.ProvedorJwt;
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

    @Autowired
    private ProvedorJwt provedorJwt;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();

            ContextoSeguranca.limpar();
            SecurityContextHolder.clearContext();

            if (path.equals("/auth/login")) {
                filterChain.doFilter(request, response);
                return;
            }

            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(7);

            if (!provedorJwt.validarJwt(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String nomeUsuario = provedorJwt.obterNomeUsuario(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(nomeUsuario);
            UsernamePasswordAuthenticationToken autenticacao = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(autenticacao);

            Usuario usuario = autenticacaoServico.validarToken(token);
            ContextoSeguranca.setUsuario(usuario);

            filterChain.doFilter(request, response);
        } finally {
            ContextoSeguranca.limpar();
            SecurityContextHolder.clearContext();
        }
    }
}
