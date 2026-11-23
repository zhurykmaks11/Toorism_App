package org.example.turism_app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.turism_app.dto.AuthRequest;
import org.example.turism_app.dto.AuthResponse;
import org.example.turism_app.exception.UnauthorizedException;
import org.example.turism_app.security.JwtService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Лаба №8 — отримання JWT.
 *
 * <pre>
 * POST /api/auth/authenticate
 * {"username": "admin", "password": "admin123"}
 *
 * → 200 {"token": "eyJhbGciOiJIUzM4NCJ9....", "type": "Bearer", "expiresIn": 86400}
 * </pre>
 *
 * Далі токен передається в кожному запиті: {@code Authorization: Bearer <token>}.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/authenticate")
    public AuthResponse authenticate(@Valid @RequestBody AuthRequest request) {
        UserDetails user;
        try {
            user = userDetailsService.loadUserByUsername(request.username());
        } catch (UsernameNotFoundException e) {
            throw new UnauthorizedException("Неправильний логін або пароль");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Неправильний логін або пароль");
        }
        return new AuthResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationSeconds());
    }
}
