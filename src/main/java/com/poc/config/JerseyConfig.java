package com.poc.config;

import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.poc.api.impl.ApiResourceConfig;

/**
 * Monte l'application JAX-RS (Jersey) comme un simple servlet sur {@code /api/*}.
 *
 * <p>C'est le SEUL point de contact entre Spring et la couche REST : Spring Boot
 * héberge le servlet, mais les controllers restent 100 % Jakarta EE. Le
 * DispatcherServlet Spring garde {@code /} (pages JSP, Swagger UI) ; Jersey,
 * plus spécifique, gagne sur {@code /api/**}.
 */
@Configuration
public class JerseyConfig {

    @Bean
    public ServletRegistrationBean<ServletContainer> jerseyServlet() {
        ServletRegistrationBean<ServletContainer> registration =
                new ServletRegistrationBean<>(new ServletContainer(new ApiResourceConfig()), "/api/*");
        registration.setName("JerseyRest");
        registration.setLoadOnStartup(1);
        return registration;
    }
}
