package org.example.turism_app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * 403 Forbidden:
 * <ul>
 *   <li>немає CSRF-токена або він недійсний (лаба №4);</li>
 *   <li>у ролі користувача недостатньо прав для цієї дії (лаби №3, №5).</li>
 * </ul>
 */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        String message;
        if (accessDeniedException instanceof CsrfException) {
            message = "Відсутній або недійсний CSRF-токен: передайте значення cookie XSRF-TOKEN у заголовку X-XSRF-TOKEN";
        } else {
            message = "Доступ заборонено: користувач " + currentUser() + " не має права виконувати "
                    + request.getMethod() + ". " + requiredRoles(request.getMethod());
        }
        SecurityErrorWriter.write(request, response, HttpStatus.FORBIDDEN, message);
    }

    /** Наприклад: «admin (ADMIN)». */
    private static String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "anonymous";
        }
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .collect(Collectors.joining(", "));
        return auth.getName() + " (" + roles + ")";
    }

    private static String requiredRoles(String method) {
        return switch (method) {
            case "POST", "PUT", "PATCH" -> "Потрібна роль ADMIN або SUPERADMIN";
            case "DELETE" -> "Потрібна роль SUPERADMIN";
            default -> "Недостатньо прав";
        };
    }
}
