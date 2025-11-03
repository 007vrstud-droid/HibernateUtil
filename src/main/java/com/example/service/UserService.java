package com.example.service;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {

    void createUser(UserCreateRequest request);

    void updateUser(UserUpdateRequest request);

    Optional<UserResponse> getUserById(Long id);

    List<UserResponse> getAllUsers();

    void deleteUser(Long id);

    boolean isEmailExists(String email);
}
