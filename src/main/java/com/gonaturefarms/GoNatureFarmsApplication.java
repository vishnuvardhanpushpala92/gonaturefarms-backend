package com.gonaturefarms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the Go Nature Farms backend.
 * <p>
 * This application replaces the original Node.js + Express + MySQL backend
 * with Spring Boot + Spring Data JPA (Hibernate) + PostgreSQL, while keeping
 * every REST endpoint path and response shape identical so the existing
 * HTML/CSS/JS frontend keeps working without any changes.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class GoNatureFarmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoNatureFarmsApplication.class, args);
    }
}
