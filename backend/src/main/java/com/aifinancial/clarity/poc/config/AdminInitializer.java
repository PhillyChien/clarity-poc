package com.aifinancial.clarity.poc.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.UserRepository;

@Configuration
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Bean
    public CommandLineRunner initSuperAdmin() {
        return args -> {
            // Check if the Super Admin user exists already
            if (!userRepository.existsByUsername("admin")) {
                User superAdmin = new User();
                superAdmin.setUsername("admin");
                superAdmin.setEmail("admin@example.com");
                superAdmin.setPassword(passwordEncoder.encode("admin123"));
                superAdmin.setRole(Role.SUPER_ADMIN);

                userRepository.save(superAdmin);
                System.out.println("Super Admin user created successfully with secure password encoding!");
            } else {
                System.out.println("Super Admin user already exists.");
            }
        };
    }
} 