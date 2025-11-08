package org.firstlab.second.config;

import org.firstlab.second.entity.Role;
import org.firstlab.second.entity.UserAccount;
import org.firstlab.second.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserAccountRepository users, PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByUsername("admin")) {
                UserAccount admin = new UserAccount();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("Admin@123"));
                admin.setEnabled(true);
                admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
                users.save(admin);
                System.out.println("[DEBUG_LOG] Seeded default admin: admin/Admin@123");
            }
            if (!users.existsByUsername("user")) {
                UserAccount user = new UserAccount();
                user.setUsername("user");
                user.setPassword(encoder.encode("User@1234"));
                user.setEnabled(true);
                user.setRoles(Set.of(Role.ROLE_USER));
                users.save(user);
                System.out.println("[DEBUG_LOG] Seeded default user: user/User@1234");
            }
        };
    }
}
