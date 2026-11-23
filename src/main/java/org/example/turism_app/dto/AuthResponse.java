package org.example.turism_app.dto;

/** Відповідь з JWT-токеном. */
public record AuthResponse(
        String token,
        String type,
        long expiresIn
) {
}
