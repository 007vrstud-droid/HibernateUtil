package com.example.validation;

import com.example.entity.UserEntity;
import com.example.exception.DuplicateResourceException;
import com.example.exception.InvalidDataException;
import com.example.repository.UserDao;

import java.time.LocalDateTime;

/**
 * Общие проверки и предусловия для пользователя.
 * Объединяет валидацию и вспомогательные предусловия.
 */
public final class UserChecks {

    private UserChecks() {
    }

    // ---------- Валидация простых полей ----------

    public static void validateUserNotNull(Object user) {
        if (user == null) {
            throw new InvalidDataException("Пользователь не может быть null");
        }
    }

    public static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("Некорректный ID: " + id);
        }
    }

    public static void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new InvalidDataException("Некорректный email: " + email);
        }
    }

    public static void validateAge(Integer age) {
        if (age != null && (age < 0 || age > 150)) {
            throw new InvalidDataException("Некорректный возраст: " + age);
        }
    }

    // ---------- Проверки уникальности ----------

    /**
     * Проверка при создании нового пользователя
     */
    public static void ensureEmailUniqueForCreate(String email, UserDao userDao) {
        userDao.findByEmail(email)
                .ifPresent(u -> {
                    throw new DuplicateResourceException(
                            "Пользователь с email " + email + " уже существует");
                });
    }

    /**
     * Проверка при обновлении существующего пользователя
     */
    public static void ensureEmailUniqueForUpdate(UserEntity user, UserDao userDao) {
        userDao.findByEmail(user.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(user.getId())) {
                        throw new DuplicateResourceException(
                                "Email " + user.getEmail() + " уже используется другим пользователем");
                    }
                });
    }

    // ---------- Присвоение значений ----------

    /**
     * Присваивает дату создания, если она отсутствует
     */
    public static void ensureCreatedAt(UserEntity user) {
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
    }
}
