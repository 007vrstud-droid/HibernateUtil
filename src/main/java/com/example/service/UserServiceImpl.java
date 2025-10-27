package com.example.service;

import com.example.model.UserEntity;
import com.example.repository.UserDao;
import com.example.repository.UserDaoImpl;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    // ✅ Конструктор для тестов и DI
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    // ✅ Конструктор по умолчанию — если кто-то создаёт вручную
    public UserServiceImpl() {
        this.userDao = new UserDaoImpl();
    }

    @Override
    public void createUser(UserEntity user) {
        if (user == null) {
            log.warn("Попытка создать null-пользователя");
            return;
        }

        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            log.warn("Некорректный email: {}", user.getEmail());
            return;
        }

        if (userDao.isEmailExists(user.getEmail())) {
            log.warn("Пользователь с email {} уже существует", user.getEmail());
            return;
        }

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        userDao.saveUser(user);
        log.info("Пользователь успешно создан: {}", user);
    }

    @Override
    public UserEntity getUserById(Long id) {
        if (id == null || id <= 0) {
            log.warn("Некорректный ID: {}", id);
            return null;
        }
        return userDao.getUser(id);
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userDao.getAllUsers();
    }

    @Override
    public void updateUser(UserEntity user) {
        if (user == null || user.getId() == null) {
            log.warn("Невозможно обновить: пользователь или ID отсутствует");
            return;
        }

        UserEntity existing = userDao.getUser(user.getId());
        if (existing == null) {
            log.warn("Пользователь с ID {} не найден", user.getId());
            return;
        }

        if (user.getEmail() != null && !user.getEmail().equals(existing.getEmail())
                && userDao.isEmailExists(user.getEmail())) {
            log.warn("Email {} уже используется другим пользователем", user.getEmail());
            return;
        }

        userDao.updateUser(user);
        log.info("Пользователь обновлён: {}", user);
    }

    @Override
    public void deleteUser(Long id) {
        if (id == null || id <= 0) {
            log.warn("Некорректный ID: {}", id);
            return;
        }

        userDao.deleteUser(id);
        log.info("Пользователь с ID {} удалён (если существовал)", id);
    }

    @Override
    public boolean isEmailExists(String email) {
        return userDao.isEmailExists(email);
    }
}
