package com.example.service;

import com.example.entity.UserEntity;
import com.example.exception.NotFoundException;
import com.example.repository.UserDao;
import com.example.validation.UserChecks;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления пользователями.
 */
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void createUser(UserEntity user) {
        UserChecks.validateUserNotNull(user);
        UserChecks.validateEmail(user.getEmail());
        UserChecks.validateAge(user.getAge());
        UserChecks.ensureEmailUniqueForCreate(user.getEmail(), userDao);

        UserChecks.ensureCreatedAt(user);

        userDao.save(user);
        log.info("Пользователь успешно создан: {}", user);
    }

    @Override
    public void updateUser(UserEntity user) {
        UserChecks.validateUserNotNull(user);
        UserChecks.validateId(user.getId());
        UserChecks.validateEmail(user.getEmail());
        UserChecks.validateAge(user.getAge());

        userDao.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + user.getId() + " не найден"));

        UserChecks.ensureEmailUniqueForUpdate(user, userDao);
        userDao.update(user);
        log.info("Пользователь обновлён: {}", user);
    }

    @Override
    public Optional<UserEntity> getUserById(Long id) {
        UserChecks.validateId(id);
        return userDao.findById(id);
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        UserChecks.validateId(id);
        userDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));

        userDao.deleteById(id);
        log.info("Пользователь с ID {} удалён", id);
    }

    @Override
    public boolean isEmailExists(String email) {
        UserChecks.validateEmail(email);
        return userDao.findByEmail(email).isPresent();
    }
}
