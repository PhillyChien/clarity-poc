package com.aifinancial.clarity.poc.config;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class FlywayProfileLogger implements CommandLineRunner {

    private final Environment environment;

    public FlywayProfileLogger(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        String[] activeProfiles = environment.getActiveProfiles();
        System.out.println("\n---------------------------------------------");
        System.out.println("Application started with profiles: " + 
            (activeProfiles.length > 0 ? Arrays.toString(activeProfiles) : "No specific profiles active, using default"));
        System.out.println("Database URL: " + environment.getProperty("spring.datasource.url"));
        System.out.println("Flyway enabled: " + environment.getProperty("spring.flyway.enabled"));
        System.out.println("Flyway locations: " + environment.getProperty("spring.flyway.locations"));
        System.out.println("---------------------------------------------\n");
    }
} 