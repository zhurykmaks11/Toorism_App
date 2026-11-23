package org.example.turism_app.exception;

/** Неправильний логін або пароль під час отримання JWT → HTTP 401. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
