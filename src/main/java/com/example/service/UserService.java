package com.example.service;

import com.example.model.UserEntity;
import java.util.List;

public interface UserService {
    void createUser(UserEntity user);
    UserEntity getUserById(Long id);
    List<UserEntity> getAllUsers();
    void updateUser(UserEntity user);
    void deleteUser(Long id);
    boolean isEmailExists(String email);
}