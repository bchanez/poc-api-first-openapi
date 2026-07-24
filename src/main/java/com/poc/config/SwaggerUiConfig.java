package com.poc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Version MINIMALE (Spring) : sert la spec riche + le webjar Swagger UI.
 *
 * <p>Swagger UI reçoit l'URL de la spec via le {@code ?url=} du redirect
 * (plugin DownloadUrl du webjar) → pas besoin de surcharger swagger-initializer.js.
 *
 * <p>⚠️ Servir la spec via un RESOURCE HANDLER (et non un @Controller qui
 * retourne une String) est ce qui évite le "Circular view path".
 *
 * <p>Handlers déclarés explicitement car {@code spring.web.resources.add-mappings=false}
 * désactive le service statique par défaut (webjars compris).
 */
@Configuration
public class SwaggerUiConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1) LA SPEC SOURCE, servie brute (fichier statique -> pas de vue -> pas de circular).
        //    ⚠️ Pattern RÉPERTOIRE + wildcard (/openapi/**) et location = le DOSSIER
        //    (classpath:/openapi/). Un pattern EXACT (/openapi/openapi.json) est fragile :
        //    s'il ne résout pas le fichier, la requête retombe en résolution de vue -> circular.
        registry.addResourceHandler("/openapi/**")
                .addResourceLocations("classpath:/openapi/");

        // 2) Le webjar Swagger UI (assets bruts). URL sans version grâce à webjars-locator.
        registry.addResourceHandler("/webjars/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/5.17.14/");
    }
}
