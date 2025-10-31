package com.example.exception;

public class NotFoundException extends RuntimeException {
    /**
     * Создаёт новое исключение, указывающее на отсутствие ресурса.
     */
    public NotFoundException(String message) {
        super(message);
    }
}
