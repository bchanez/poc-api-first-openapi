package com.poc;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point when the WAR is deployed to an external Tomcat.
 * Tomcat discovers this class (via SpringServletContainerInitializer) and
 * boots the Spring context through it instead of PocApplication#main.
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PocApplication.class);
    }
}
