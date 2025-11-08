package org.firstlab.second.controller;

import jakarta.validation.Valid;
import org.firstlab.second.dto.RegisterRequest;
import org.firstlab.second.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        Map<String, String> map = new HashMap<>();
        map.put("headerName", token.getHeaderName());
        map.put("parameterName", token.getParameterName());
        map.put("token", token.getToken());
        return map;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> result = authService.register(request);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        if (authentication == null) {
            return Map.of("authenticated", false);
        }
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return Map.of(
                "authenticated", true,
                "username", authentication.getName(),
                "roles", roles
        );
    }
}
