package com.aifinancial.clarity.poc.config;

import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

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
                User superAdmin = User.builder()
                        .username("admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.SUPER_ADMIN)
                        .build();

                userRepository.save(superAdmin);
                System.out.println("Super Admin user created successfully!");
            } else {
                System.out.println("Super Admin user already exists.");
            }
        };
    }
} 