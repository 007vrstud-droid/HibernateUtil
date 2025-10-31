package com.example.service;

import com.example.entity.UserEntity;
import java.util.List;
import java.util.Optional;

/**
 * Сервисный слой для управления пользователями.
 */
public interface UserService {

    /**
     * Создаёт нового пользователя в системе.
     */
    void createUser(UserEntity user);

    /**
     * Возвращает пользователя по его идентификатору.
     */
    Optional<UserEntity> getUserById(Long id);

    /**
     * Возвращает список всех пользователей в системе.
     */
    List<UserEntity> getAllUsers();

    /**
     * Обновляет существующего пользователя.
     */
    void updateUser(UserEntity user);

    /**
     * Удаляет пользователя из системы по его идентификатору.
     */
    void deleteUser(Long id);

    /**
     * Проверяет, существует ли пользователь с указанным адресом электронной почты.
     */
    boolean isEmailExists(String email);
}
