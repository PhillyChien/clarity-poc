package com.aifinancial.clarity.poc.config;

import java.util.Arrays;

import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class FlywayConfig {

    private final Environment environment;

    public FlywayConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public Callback flywayCallback() {
        return new Callback() {
            @Override
            public boolean supports(Event event, Context context) {
                return event == Event.AFTER_MIGRATE;
            }

            @Override
            public boolean canHandleInTransaction(Event event, Context context) {
                return true;
            }

            @Override
            public void handle(Event event, Context context) {
                if (event == Event.AFTER_MIGRATE) {
                    String[] activeProfiles = environment.getActiveProfiles();
                    System.out.println("\n---------------------------------------------");
                    System.out.println("Flyway migration completed successfully!");
                    System.out.println("Active profiles: " + 
                        (activeProfiles.length > 0 ? Arrays.toString(activeProfiles) : "No specific profiles active, using default"));
                    System.out.println("Database URL: " + environment.getProperty("spring.datasource.url"));
                    System.out.println("---------------------------------------------\n");
                }
            }

            @Override
            public String getCallbackName() {
                return "profileInfoCallback";
            }
        };
    }
} 