package com.example.service;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.entity.UserEntity;
import com.example.exception.NotFoundException;
import com.example.mapper.UserMapper;
import com.example.repository.UserRepository;
import com.example.util.UserChecks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserChecks userChecks;
    private final UserMapper userMapper;


    @Override
    public void createUser(UserCreateRequest request) {
        log.debug("Попытка создания пользователя с email: {}", request.getEmail());

        userChecks.validateUserNotNull(request);
        userChecks.validateEmail(request.getEmail());
        userChecks.validateAge(request.getAge());

        UserEntity user = new UserEntity();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());

        userRepository.save(user);
        log.info("Пользователь успешно создан: {}", user);
    }

    @Override
    public void updateUser(UserUpdateRequest request) {
        log.debug("Попытка обновления пользователя с id: {}", request.getId());
        userChecks.validateId(request.getId());
        userChecks.validateUserNotNull(request);
        userChecks.validateEmail(request.getEmail());
        userChecks.validateAge(request.getAge());

        UserEntity existing = userRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + request.getId() + " не найден"));

        // Проверка уникальности email
        UserEntity temp = new UserEntity();
        temp.setId(request.getId());
        temp.setEmail(request.getEmail());
        userChecks.ensureEmailUniqueForUpdate(temp);

        // Обновление данных
        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        existing.setAge(request.getAge());

        userRepository.save(existing);
        log.info("Пользователь обновлён: {}", existing);
    }

    @Override
    public Optional<UserResponse> getUserById(Long id) {
        log.debug("Поиск пользователя с id: {}", id);
        userChecks.validateId(id);
        return userRepository.findById(id).map(userMapper::toResponse);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.debug("Получение списка всех пользователей");
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Попытка удалить пользователя с id: {}", id);
        userChecks.validateId(id);
        userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} удалён", id);
    }

    @Override
    public boolean isEmailExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        return userOpt.isPresent();
    }
}
