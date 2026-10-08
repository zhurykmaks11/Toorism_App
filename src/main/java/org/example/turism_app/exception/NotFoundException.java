package org.example.turism_app.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String entity, Long id) {
        super(entity + " з id=" + id + " не знайдено");
    }
}
