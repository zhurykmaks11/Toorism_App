package org.example.turism_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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
 * <p><b>Лаба №3 — Basic Authorization.</b> Усі CRUD-запити потребують логін і пароль.</p>
 *
 * <p><b>Лаба №5 — Multi-user.</b> Три користувачі з різними правами:
 * <table>
 *   <tr><th>Роль</th><th>GET</th><th>POST / PUT / PATCH</th><th>DELETE</th></tr>
 *   <tr><td>USER</td><td>так</td><td>ні (403)</td><td>ні (403)</td></tr>
 *   <tr><td>ADMIN</td><td>так</td><td>так</td><td>ні (403)</td></tr>
 *   <tr><td>SUPERADMIN</td><td>так</td><td>так</td><td>так</td></tr>
 * </table></p>
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

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SUPERADMIN = "SUPERADMIN";

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
                        // Читання — усі три ролі
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole(ROLE_USER, ROLE_ADMIN, ROLE_SUPERADMIN)
                        // Створення і редагування — ADMIN і SUPERADMIN
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole(ROLE_ADMIN, ROLE_SUPERADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole(ROLE_ADMIN, ROLE_SUPERADMIN)
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole(ROLE_ADMIN, ROLE_SUPERADMIN)
                        // Видалення — лише SUPERADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole(ROLE_SUPERADMIN)
                        // Усе інше в /api/** — заборонено
                        .requestMatchers("/api/**").denyAll()
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

    /** Лаба №5: три користувачі в пам'яті; логіни й паролі — з application.properties. */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder,
                                                 @Value("${app.security.user.username}") String userUsername,
                                                 @Value("${app.security.user.password}") String userPassword,
                                                 @Value("${app.security.admin.username}") String adminUsername,
                                                 @Value("${app.security.admin.password}") String adminPassword,
                                                 @Value("${app.security.superadmin.username}") String superadminUsername,
                                                 @Value("${app.security.superadmin.password}") String superadminPassword) {
        UserDetails user = User.builder()
                .username(userUsername)
                .password(encoder.encode(userPassword))
                .roles(ROLE_USER)
                .build();

        UserDetails admin = User.builder()
                .username(adminUsername)
                .password(encoder.encode(adminPassword))
                .roles(ROLE_ADMIN)
                .build();

        UserDetails superadmin = User.builder()
                .username(superadminUsername)
                .password(encoder.encode(superadminPassword))
                .roles(ROLE_SUPERADMIN)
                .build();

        return new InMemoryUserDetailsManager(user, admin, superadmin);
    }
}
