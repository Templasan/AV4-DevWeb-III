package com.autobots.automanager.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ConfiguracaoSeguranca {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            // IF_REQUIRED: sessions criadas pelo framework de testes (MockMvc / @WithMockUser)
            // funcionam normalmente; em produção, o filtro JWT autentica por requisição e
            // nunca cria sessão na prática.
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .and()
            .authorizeRequests()
                .antMatchers("/auth/login").permitAll()
                .anyRequest().permitAll()
            .and()
            .httpBasic().disable()
            .formLogin().disable();
        return http.build();
    }

    /**
     * BCryptPasswordEncoder para hash seguro de senhas.
     * Strength=10 oferece bom balanço entre segurança e performance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
