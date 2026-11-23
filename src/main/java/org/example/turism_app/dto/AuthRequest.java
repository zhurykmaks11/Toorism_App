package org.example.turism_app.dto;

import jakarta.validation.constraints.NotBlank;

/** Тіло запиту POST /api/auth/authenticate. */
public record AuthRequest(
        @NotBlank(message = "Логін обов'язковий")
        String username,

        @NotBlank(message = "Пароль обов'язковий")
        String password
) {
}
