package com.example.util;

import com.example.entity.UserEntity;
import com.example.exception.DuplicateResourceException;
import com.example.exception.InvalidDataException;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Общие проверки и предусловия для пользователя.
 * Объединяет валидацию и вспомогательные предусловия.
 */
@Component
@RequiredArgsConstructor
public final class UserChecks {
    private final UserRepository userRepository;

    public void validateUserNotNull(Object user) {
        if (user == null) {
            throw new InvalidDataException("Пользователь не может быть null");
        }
    }

    public void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("Некорректный ID: " + id);
        }
    }

    public void validateEmail(String email) {
        if (email == null) {
            throw new InvalidDataException("Email не может быть null");
        }

        String emailRegex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);

        if (!pattern.matcher(email).matches()) {
            throw new InvalidDataException("Некорректный email: " + email);
        }
    }

    public void validateAge(Integer age) {
        if (age != null && (age < 0 || age > 150)) {
            throw new InvalidDataException("Некорректный возраст: " + age);
        }
    }

    /**
     * Проверка при создании нового пользователя
     */
    public void ensureEmailUniqueForCreate(String email) {
        userRepository.findByEmail(email)
                .ifPresent(u -> {
                    throw new DuplicateResourceException(
                            "Пользователь с email " + email + " уже существует");
                });
    }

    /**
     * Проверка при обновлении существующего пользователя
     */
    public void ensureEmailUniqueForUpdate(UserEntity user) {
        userRepository.findByEmail(user.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(user.getId())) {
                        throw new DuplicateResourceException(
                                "Email " + user.getEmail() + " уже используется другим пользователем");
                    }
                });
    }
}
