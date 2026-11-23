package org.example.turism_app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Лаба №8 — вхід за JWT-токеном.
 *
 * <p>Якщо в запиті є заголовок {@code Authorization: Bearer <token>}, фільтр перевіряє підпис
 * і термін дії токена, бере з нього логін (sub) і ролі (authorities) і вважає користувача
 * авторизованим. Пароль при цьому не передається і база користувачів не перевіряється —
 * сервер довіряє власному підпису.</p>
 *
 * <p>Недійсний або прострочений токен → 401 Unauthorized.
 * Запити з Basic Auth (лаби №3–7) фільтр не чіпає.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtService jwtService, AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtService = jwtService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /** Чи містить запит JWT-токен (використовується також для винятку з CSRF-перевірки). */
    public static boolean hasBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        return header != null && header.startsWith(BEARER_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!hasBearerToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(HttpHeaders.AUTHORIZATION).substring(BEARER_PREFIX.length()).trim();
        try {
            Claims claims = jwtService.parseToken(token);

            List<SimpleGrantedAuthority> authorities = rolesFrom(claims).stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(claims.getSubject(), null, authorities);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException("Недійсний або прострочений JWT-токен", e));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static List<String> rolesFrom(Claims claims) {
        Object value = claims.get(JwtService.CLAIM_AUTHORITIES);
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
