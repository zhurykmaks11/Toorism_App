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

/**
 * Лабораторна №3: захист усіх CRUD-запитів з лаби №2 через HTTP Basic Authorization.
 *
 * <ul>
 *   <li>Без логіна/пароля або з неправильним паролем → 401 Unauthorized</li>
 *   <li>USER  — може лише читати (GET /api/**)</li>
 *   <li>ADMIN — може все: GET, POST, PUT, PATCH, DELETE</li>
 *   <li>USER намагається змінити дані → 403 Forbidden</li>
 * </ul>
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
                // REST API без форм і cookie-сесій: CSRF-захист не потрібен,
                // інакше POST/PUT/DELETE з Postman отримували б 403
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Публічні: перевірка стану сервера і статичні файли (index.html)
                        .requestMatchers("/api/health").permitAll()
                        // Читання — для USER і ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                        // Створення, оновлення, видалення — лише ADMIN
                        .requestMatchers("/api/**").hasRole(ROLE_ADMIN)
                        .anyRequest().permitAll()
                )

                // Увімкнути Basic Authorization (заголовок Authorization: Basic base64(login:password))
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)   // 401
                        .accessDeniedHandler(accessDeniedHandler)             // 403
                );

        return http.build();
    }

    /** Паролі зберігаються у вигляді BCrypt-хешу, а не відкритим текстом. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Користувачі зберігаються в пам'яті; логіни й паролі беруться з application.properties. */
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
