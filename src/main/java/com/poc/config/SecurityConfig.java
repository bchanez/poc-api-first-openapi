package com.poc.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security : il faut ouvrir TOUTES les URLs dont Swagger UI a besoin,
 * pas seulement la spec. Swagger charge en réalité :
 *   - le redirect            : /swagger
 *   - les assets du webjar    : /webjars/swagger-ui/**  (index.html, *.js, *.css)
 *   - la spec elle-même       : /openapi.yaml
 * Si UNE seule est bloquée (401/403), l'UI reste blanche ou affiche "error".
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 👇 les 3 familles d'URL indispensables à Swagger UI
                .requestMatchers(
                        "/",                        // racine -> redirige vers Swagger UI
                        "/swagger-ui",              // la route du controller (doit matcher EXACTEMENT)
                        "/openapi/**",              // le DOSSIER de la spec (pas le fichier exact)
                        "/webjars/swagger-ui/**"
                ).permitAll()
                // le reste protégé (exemple : l'API réclame une auth)
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults())
            // POC : on désactive CSRF pour pouvoir tester l'API au curl
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
