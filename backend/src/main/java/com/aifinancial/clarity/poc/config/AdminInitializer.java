package com.aifinancial.clarity.poc.config;

import java.util.NoSuchElementException;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.aifinancial.clarity.poc.constant.RoleConstants;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.RoleRepository;
import com.aifinancial.clarity.poc.repository.UserRepository;

@Configuration
public class AdminInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initSuperAdmin() {
        return args -> {
            // Fetch the SUPER_ADMIN role using the constant
            Role superAdminRole = roleRepository.findByName(RoleConstants.ROLE_SUPER_ADMIN)
                    .orElseThrow(() -> new NoSuchElementException("SUPER_ADMIN role not found in database. Please initialize roles first."));

            // Check if the Super Admin user exists already
            if (!userRepository.existsByUsername("admin")) {
                User superAdmin = new User();
                superAdmin.setUsername("admin");
                superAdmin.setEmail("admin@example.com");
                superAdmin.setPassword(passwordEncoder.encode("admin123"));
                superAdmin.setRole(superAdminRole);

                userRepository.save(superAdmin);
                System.out.println("Super Admin user created successfully with secure password encoding!");
            } else {
                System.out.println("Super Admin user already exists.");
            }
        };
    }
} 