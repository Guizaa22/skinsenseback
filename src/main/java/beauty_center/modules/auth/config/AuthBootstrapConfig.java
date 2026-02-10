package beauty_center.modules.auth.config;

import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

/**
 * Bootstrap configuration for test users.
 * Only active in 'dev' and 'test' profiles.
 * Creates sample users if they don't exist.
 */
@Configuration
@Profile({"dev", "test"})
@RequiredArgsConstructor
@Slf4j
public class AuthBootstrapConfig {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner bootstrapTestUsers() {
        return args -> {
            log.info("=== Bootstrapping Test Users ===");

            // Admin user
            createUserIfNotExists(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                "Admin User",
                "admin@beautycenter.com",
                "+1-555-0001",
                "Admin@123",
                Role.ADMIN
            );

            // Employee user
            createUserIfNotExists(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440001"),
                "Staff Employee",
                "employee@beautycenter.com",
                "+1-555-0002",
                "Employee@123",
                Role.EMPLOYEE
            );

            // Client user
            createUserIfNotExists(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440002"),
                "Regular Client",
                "client@beautycenter.com",
                "+1-555-0003",
                "Client@123",
                Role.CLIENT
            );

            log.info("=== Test Users Bootstrap Complete ===");
        };
    }

    private void createUserIfNotExists(UUID id, String fullName, String email, String phone, String password, Role role) {
        if (userAccountRepository.findByEmail(email).isEmpty()) {
            UserAccount user = UserAccount.builder()
                .id(id)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(password))
                .active(true)
                .role(role)
                .build();

            userAccountRepository.save(user);
            log.info("Created test user: {} ({})", email, role);
        } else {
            log.debug("User already exists: {}", email);
        }
    }

}
