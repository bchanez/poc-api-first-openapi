package com.poc.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * /swagger-ui -> Swagger UI (webjar) pointé sur la spec SOURCE, SANS coder le
 * nom du WAR en dur.
 *
 * <p>Deux astuces qui rendent l'URL indépendante du context path :
 *  - la cible du redirect ({@code /webjars/...}) : un {@code redirect:/...} est
 *    interprété par Spring comme relatif au context path → il préfixe /monwar
 *    tout seul.
 *  - le paramètre {@code ?url=} : Spring NE le réécrit PAS (c'est une query
 *    opaque). On y injecte donc {@code request.getContextPath()} à la main.
 */
@Controller
public class SwaggerRedirectController {

    @GetMapping("/swagger-ui")
    public String swagger(HttpServletRequest request) {
        String ctx = request.getContextPath(); // "/monwar" en prod, "" si ROOT
        return "redirect:/webjars/swagger-ui/index.html?url=" + ctx + "/openapi/openapi.yaml";
    }

    /** Racine -> Swagger UI (la home JSP a été retirée du POC). */
    @GetMapping("/")
    public String root() {
        return "redirect:/swagger-ui";
    }
}
