package org.example.turism_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Налаштування безпеки API.
 *
 * <p><b>Лаба №3 — Basic Authorization.</b> Усі CRUD-запити потребують логін і пароль:
 * USER — лише читання (GET), ADMIN — усі операції.</p>
 *
 * <p><b>Лаба №4 — CSRF.</b> Запити, що змінюють дані (POST, PUT, PATCH, DELETE), мають містити CSRF-токен:
 * <ol>
 *   <li>сервер видає токен у cookie {@code XSRF-TOKEN} (у відповідь на будь-який запит);</li>
 *   <li>клієнт повертає цей токен у заголовку {@code X-XSRF-TOKEN} разом із cookie;</li>
 *   <li>якщо токена немає або він не збігається з cookie → 403 Forbidden.</li>
 * </ol>
 * Так сторонній сайт не зможе виконати запит від імені користувача: він не має доступу до значення cookie.</p>
 *
 * <p><b>Лаба №4 — Audit.</b> {@link AuditLogFilter} записує в журнал кожен запит до API
 * (користувач, метод, адреса, код відповіді), а JPA Auditing зберігає в кожному записі,
 * хто і коли його створив та змінив (див. {@link JpaAuditingConfig}).</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JsonAuthenticationEntryPoint authenticationEntryPoint,
                                                   JsonAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                // ---------- CSRF ----------
                // Токен зберігається в cookie XSRF-TOKEN (httpOnly=false, щоб клієнт міг його прочитати)
                // і очікується в заголовку X-XSRF-TOKEN. Обробник без маскування токена:
                // у заголовку передається те саме значення, що й у cookie.
                .csrf(csrf -> csrf
                        // Cookie XSRF-TOKEN, яка не стирається при кожній Basic-авторизації
                        .csrfTokenRepository(new StatelessCookieCsrfTokenRepository())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ---------- Авторизація (лаба №3) ----------
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                        .requestMatchers("/api/**").hasRole(ROLE_ADMIN)
                        .anyRequest().permitAll()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)   // 401
                        .accessDeniedHandler(accessDeniedHandler)             // 403 (роль або CSRF)
                )

                // Журнал аудиту — обгортає всю перевірку безпеки, тому бачить фінальний код відповіді
                .addFilterBefore(new AuditLogFilter(), CsrfFilter.class)
                // Видає cookie XSRF-TOKEN у кожній відповіді (Spring Security 6+ створює токен "ліниво")
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder,
                                                 @Value("${app.security.admin.username}") String adminUsername,
                                                 @Value("${app.security.admin.password}") String adminPassword,
                                                 @Value("${app.security.user.username}") String userUsername,
                                                 @Value("${app.security.user.password}") String userPassword) {
        return new InMemoryUserDetailsManager(
                User.withUsername(adminUsername)
                        .password(encoder.encode(adminPassword))
                        .roles(ROLE_ADMIN)
                        .build(),
                User.withUsername(userUsername)
                        .password(encoder.encode(userPassword))
                        .roles(ROLE_USER)
                        .build()
        );
    }
}
