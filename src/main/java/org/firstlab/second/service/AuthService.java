package org.firstlab.second.service;

import org.firstlab.second.dto.LoginRequest;
import org.firstlab.second.dto.RefreshTokenRequest;
import org.firstlab.second.dto.RegisterRequest;
import org.firstlab.second.dto.TokenResponse;
import org.firstlab.second.entity.Role;
import org.firstlab.second.entity.SessionStatus;
import org.firstlab.second.entity.UserAccount;
import org.firstlab.second.entity.UserSession;
import org.firstlab.second.repository.UserAccountRepository;
import org.firstlab.second.repository.UserSessionRepository;
import org.firstlab.second.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

@Service
public class AuthService {

    private final UserAccountRepository userRepo;
    private final UserSessionRepository sessionRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserAccountRepository userRepo,
                       UserSessionRepository sessionRepo,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.sessionRepo = sessionRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
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

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // Аутентифицируем пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername().trim().toLowerCase(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserAccount user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Генерируем токены
        return createTokenPair(user, userDetails, ipAddress, userAgent);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Валидируем refresh токен
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // Хэшируем токен для поиска в БД
        String tokenHash = hashToken(refreshToken);

        // Ищем сессию
        UserSession session = sessionRepo.findByRefreshTokenHashAndStatus(tokenHash, SessionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Session not found or already used"));

        // Помечаем старую сессию как REFRESHED
        session.setStatus(SessionStatus.REFRESHED);
        sessionRepo.save(session);

        // Получаем пользователя
        UserAccount user = session.getUser();
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(Enum::name)
                        .toArray(String[]::new))
                .build();

        // Создаем новую пару токенов
        return createTokenPair(user, userDetails, session.getIpAddress(), session.getUserAgent());
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String tokenHash = hashToken(refreshToken);
        sessionRepo.findByRefreshTokenHash(tokenHash).ifPresent(session -> {
            session.setStatus(SessionStatus.REVOKED);
            sessionRepo.save(session);
        });
    }

    @Transactional
    public void logoutAll(String username) {
        UserAccount user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        sessionRepo.revokeAllActiveSessionsForUser(user, SessionStatus.REVOKED);
    }

    private TokenResponse createTokenPair(UserAccount user, UserDetails userDetails, String ipAddress, String userAgent) {
        String sessionId = jwtTokenProvider.generateSessionId();

        // Генерируем токены
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails, user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails, user.getId(), sessionId);

        // Сохраняем сессию
        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshTokenHash(hashToken(refreshToken));
        session.setIssuedAt(Instant.now());
        session.setExpiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs()));
        session.setStatus(SessionStatus.ACTIVE);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        sessionRepo.save(session);

        return new TokenResponse(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationMs() / 1000,
                jwtTokenProvider.getRefreshTokenExpirationMs() / 1000
        );
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
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
