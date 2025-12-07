package org.firstlab.second.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        // Ensure secret is at least 256 bits (32 bytes) for HS256
        byte[] keyBytes;
        if (secret.length() < 32) {
            // Pad the secret if too short
            secret = secret + "0".repeat(32 - secret.length());
        }
        keyBytes = secret.getBytes();
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    /**
     * Генерирует Access Token с дополнительными claims
     */
    public String generateAccessToken(UserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        claims.put("userId", userId);
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Генерирует Refresh Token с уникальным идентификатором сессии
     */
    public String generateRefreshToken(UserDetails userDetails, Long userId, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("userId", userId);
        claims.put("sessionId", sessionId);

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Генерирует уникальный идентификатор сессии
     */
    public String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Извлекает username из токена
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getPayload().getSubject();
    }

    /**
     * Извлекает userId из токена
     */
    public Long getUserIdFromToken(String token) {
        Object userId = parseToken(token).getPayload().get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * Извлекает тип токена (access/refresh)
     */
    public String getTokenType(String token) {
        return (String) parseToken(token).getPayload().get("type");
    }

    /**
     * Извлекает sessionId из refresh токена
     */
    public String getSessionIdFromToken(String token) {
        return (String) parseToken(token).getPayload().get("sessionId");
    }

    /**
     * Проверяет валидность Access токена
     */
    public boolean validateAccessToken(String token) {
        try {
            Jws<Claims> claims = parseToken(token);
            String type = (String) claims.getPayload().get("type");
            return "access".equals(type) && !isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Проверяет валидность Refresh токена
     */
    public boolean validateRefreshToken(String token) {
        try {
            Jws<Claims> claims = parseToken(token);
            String type = (String) claims.getPayload().get("type");
            return "refresh".equals(type) && !isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Проверяет общую валидность токена (подпись, формат)
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Получает время истечения токена
     */
    public Date getExpirationFromToken(String token) {
        return parseToken(token).getPayload().getExpiration();
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }

    private boolean isTokenExpired(Jws<Claims> claims) {
        return claims.getPayload().getExpiration().before(new Date());
    }
}

