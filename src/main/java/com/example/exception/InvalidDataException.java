package com.example.exception;

public class InvalidDataException extends RuntimeException {
    /**
     * Создаёт новое исключение, указывающее на наличие некорректных данных.
     */
    public InvalidDataException(String message) {
        super(message);
    }
}
