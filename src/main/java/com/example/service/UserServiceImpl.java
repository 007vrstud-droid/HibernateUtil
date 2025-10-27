package com.example.service;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserUpdateRequest;
import com.example.dto.UserResponse;
import com.example.entity.UserEntity;
import com.example.exception.NotFoundException;
import com.example.repository.UserDao;
import com.example.util.UserChecks;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void createUser(UserCreateRequest request) {
        UserChecks.validateUserNotNull(request);
        UserChecks.validateEmail(request.getEmail());
        UserChecks.validateAge(request.getAge());
        UserChecks.ensureEmailUniqueForCreate(request.getEmail(), userDao);

        UserEntity user = new UserEntity();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());

        userDao.save(user);
        log.info("Пользователь успешно создан: {}", user);
    }

    @Override
    public void updateUser(UserUpdateRequest request) {
        UserChecks.validateUserNotNull(request);
        UserChecks.validateId(request.getId());
        UserChecks.validateEmail(request.getEmail());
        UserChecks.validateAge(request.getAge());

        UserEntity existing = userDao.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + request.getId() + " не найден"));

        // Проверка уникальности email
        UserEntity temp = new UserEntity();
        temp.setId(request.getId());
        temp.setEmail(request.getEmail());
        UserChecks.ensureEmailUniqueForUpdate(temp, userDao);

        // Обновление данных
        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        existing.setAge(request.getAge());

        userDao.update(existing);
        log.info("Пользователь обновлён: {}", existing);
    }

    @Override
    public Optional<UserResponse> getUserById(Long id) {
        UserChecks.validateId(id);
        return userDao.findById(id).map(this::mapToResponse);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userDao.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
        if (email == null || email.isBlank()) {
            return false;
        }

        Optional<UserEntity> userOpt = userDao.findByEmail(email);
        return userOpt.isPresent();
    }

    // ---------- MAPPING ----------
    private UserResponse mapToResponse(UserEntity entity) {
        UserResponse response = new UserResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setEmail(entity.getEmail());
        response.setAge(entity.getAge());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
