package org.example.turism_app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Лаба №8 — створення і перевірка JWT (JSON Web Token).
 *
 * <p>Токен складається з трьох частин {@code header.payload.signature}:
 * <ul>
 *   <li><b>header</b> — алгоритм підпису: {@code {"alg": "HS384"}};</li>
 *   <li><b>payload</b> — дані (claims): sub, fullName, authorities, iat, exp;</li>
 *   <li><b>signature</b> — HMAC-SHA384 від header і payload, підписаний секретним ключем сервера.</li>
 * </ul>
 * Header і payload лише закодовані в Base64URL (їх може прочитати будь-хто, напр. jwt.io),
 * а підпис гарантує, що їх ніхто не змінив. Тому в токен не можна класти паролі.</p>
 */
@Service
public class JwtService {

    public static final String CLAIM_FULL_NAME = "fullName";
    public static final String CLAIM_AUTHORITIES = "authorities";

    /** Повні імена користувачів для поля fullName у токені. */
    private static final Map<String, String> FULL_NAMES = Map.of(
            "user", "Мандрівник",
            "admin", "Адміністратор",
            "superadmin", "Суперадміністратор"
    );

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-seconds}") long expirationSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    /** Створює підписаний токен для користувача. */
    public String generateToken(UserDetails user) {
        Instant now = Instant.now();
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .toList();

        return Jwts.builder()
                .subject(user.getUsername())                                          // sub
                .claim(CLAIM_FULL_NAME, FULL_NAMES.getOrDefault(user.getUsername(), user.getUsername()))
                .claim(CLAIM_AUTHORITIES, roles)                                      // напр. ["ADMIN"]
                .issuedAt(Date.from(now))                                             // iat
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))           // exp
                .signWith(key, Jwts.SIG.HS384)                                        // alg: HS384
                .compact();
    }

    /**
     * Перевіряє підпис і термін дії токена та повертає його дані.
     *
     * @throws JwtException якщо токен підроблений, пошкоджений або прострочений
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
