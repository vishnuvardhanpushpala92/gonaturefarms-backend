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
 * 
 * Also configures CORS to allow the Vercel frontend to access the API.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.frontend-url:https://gonaturefarms-frontend-fvn3mbf18-gonatuefarms.vercel.app}")
    private String frontendUrl;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(frontendUrl)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + location);
    }
}
