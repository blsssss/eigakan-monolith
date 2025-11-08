package org.firstlab.second.service;

import org.firstlab.second.dto.RegisterRequest;
import org.firstlab.second.entity.Role;
import org.firstlab.second.entity.UserAccount;
import org.firstlab.second.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
public class AuthService {

    private final UserAccountRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest req) {
        String username = req.getUsername().trim().toLowerCase();
        String password = req.getPassword();

        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }

        validatePasswordStrength(password);

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setRoles(Set.of(Role.ROLE_USER));

        userRepo.save(user);

        return Map.of(
                "message", "User registered successfully",
                "username", username,
                "roles", user.getRoles()
        );
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        // Must contain at least one special symbol (non-alphanumeric)
        if (!password.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one special symbol");
        }
        // At least one digit
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }
}
