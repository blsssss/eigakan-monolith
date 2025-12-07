package org.firstlab.second.config;

import org.firstlab.second.entity.Role;
import org.firstlab.second.entity.UserAccount;
import org.firstlab.second.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Value("${APP_ADMIN_USERNAME:}")
    private String adminUsername;

    @Value("${APP_ADMIN_PASSWORD:}")
    private String adminPassword;

    @Value("${APP_USER_USERNAME:}")
    private String userUsername;

    @Value("${APP_USER_PASSWORD:}")
    private String userPassword;

    @Bean
    CommandLineRunner initUsers(UserAccountRepository users, PasswordEncoder encoder) {
        return args -> {
            // Seed admin from environment variables if provided
            if (StringUtils.hasText(adminUsername) && StringUtils.hasText(adminPassword)) {
                if (!users.existsByUsername(adminUsername)) {
                    UserAccount admin = new UserAccount();
                    admin.setUsername(adminUsername);
                    admin.setPassword(encoder.encode(adminPassword));
                    admin.setEnabled(true);
                    admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
                    users.save(admin);
                    System.out.println("[DEBUG_LOG] Seeded admin from env: " + adminUsername);
                } else {
                    System.out.println("[DEBUG_LOG] Admin user already exists: " + adminUsername + ", skip seeding");
                }
            } else {
                System.out.println("[DEBUG_LOG] APP_ADMIN_USERNAME/APP_ADMIN_PASSWORD not set — skip admin seeding");
            }

            // Seed regular user from environment variables if provided
            if (StringUtils.hasText(userUsername) && StringUtils.hasText(userPassword)) {
                if (!users.existsByUsername(userUsername)) {
                    UserAccount user = new UserAccount();
                    user.setUsername(userUsername);
                    user.setPassword(encoder.encode(userPassword));
                    user.setEnabled(true);
                    user.setRoles(Set.of(Role.ROLE_USER));
                    users.save(user);
                    System.out.println("[DEBUG_LOG] Seeded user from env: " + userUsername);
                } else {
                    System.out.println("[DEBUG_LOG] Regular user already exists: " + userUsername + ", skip seeding");
                }
            } else {
                System.out.println("[DEBUG_LOG] APP_USER_USERNAME/APP_USER_PASSWORD not set — skip user seeding");
            }
        };
    }
}
