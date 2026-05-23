package com.autobots.automanager.configuracao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.autobots.automanager.seguranca.FiltroSeguranca;

@Configuration
public class ConfiguracaoSeguranca {

    @Autowired
    private FiltroSeguranca filtroSeguranca;

    @Bean
    public FilterRegistrationBean<FiltroSeguranca> registroFiltroSeguranca() {
        FilterRegistrationBean<FiltroSeguranca> registro = new FilterRegistrationBean<>();
        registro.setFilter(filtroSeguranca);
        registro.addUrlPatterns("/*");
        registro.setOrder(1);
        return registro;
    }
}
