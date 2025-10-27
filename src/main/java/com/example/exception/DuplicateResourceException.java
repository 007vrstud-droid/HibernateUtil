package com.example.exception;

/**
 * Ошибка при попытке создать или обновить ресурс с уже существующими уникальными данными.
 */
public class DuplicateResourceException extends InvalidDataException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
