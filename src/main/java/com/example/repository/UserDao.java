package com.example.repository;

import com.example.model.UserEntity;

import java.util.List;

public interface UserDao {
     void saveUser(UserEntity user);
     UserEntity getUser(Long id);
     List<UserEntity> getAllUsers();
     void updateUser(UserEntity user);
     void deleteUser(Long id);
     boolean isEmailExists(String email);
}
