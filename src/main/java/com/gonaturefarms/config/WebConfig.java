package com.gonaturefarms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves uploaded product/slide images from an external, writable directory on disk
 * (equivalent to `app.use('/uploads', express.static(...))` in server.js). Unlike the
 * frontend's static index.html/script.js (bundled into the jar under
 * src/main/resources/static and served automatically by Spring Boot), uploaded files
 * are written at runtime and must live outside the jar.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.frontend-url:https://gonaturefarms-frontend-production.up.railway.app}")
    private String frontendUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + location);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Use specific origin when credentials are enabled (cannot use wildcard with credentials)
        String[] allowedOrigins;
        if (frontendUrl == null || frontendUrl.isBlank() || "*".equals(frontendUrl)) {
            allowedOrigins = new String[]{"*"};
        } else {
            allowedOrigins = new String[]{frontendUrl};
        }
        
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
                .allowCredentials(allowedOrigins.length == 1 && !"*".equals(allowedOrigins[0]))
                .maxAge(3600);
    }
}
