package com.poc.api.impl;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

/**
 * Configuration JAX-RS pure (Jersey). Point d'assemblage de la couche REST.
 * Aucun lien Spring : c'est une {@link jakarta.ws.rs.core.Application}.
 */
public class ApiResourceConfig extends ResourceConfig {

    public ApiResourceConfig() {
        // Découverte automatique des providers du package (JacksonObjectMapperProvider,
        // ValidationExceptionMapper...). C'est le rôle de packages() : trouver les
        // @Path/@Provider. À NE PAS confondre avec un scanner de spec.
        packages(true, "com.poc.api.impl");

        // ⚠️ Gotcha jaxrs-spec + interfaceOnly : le @Path du controller est sur
        // l'INTERFACE générée, pas sur la classe d'impl. packages() ne scanne que
        // les @Path DIRECTS → il rate le controller. On l'enregistre explicitement.
        register(LivreController.class);

        // ⛔ PAS de OpenApiResource : la spec n'est PAS reconstruite depuis le code.
        //    Ici elle est servie par Spring (SwaggerUiConfig), telle quelle.

        // JSON via Jackson (le module java.time est fourni par JacksonObjectMapperProvider,
        // découvert par packages()).
        register(JacksonFeature.class);

        // ⛔ PAS de OpenApiResource : on ne reconstruit AUCUNE spec depuis le code.
        //    Le contrat est servi tel quel par OpenApiSpecResource (/api/openapi.yaml).
        // register(io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class); // <-- supprimé

        // Bean Validation : renvoyer le détail des violations dans la réponse.
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
    }
}
