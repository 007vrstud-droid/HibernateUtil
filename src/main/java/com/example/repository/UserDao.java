package com.example.repository;

import com.example.entity.UserEntity;
import java.util.Optional;
import java.util.List;

/**
 * DAO для работы с пользователями.
 */
public interface UserDao {

     /**
      * Сохраняет нового пользователя в базе данных.
      */
     void save(UserEntity user);

     /**
      * Находит пользователя по ID.
      */
     Optional<UserEntity> findById(Long id);

     /**
      * Возвращает список всех пользователей.
      */
     List<UserEntity> findAll();

     /**
      * Обновляет существующего пользователя.
      */
     void update(UserEntity user);

     /**
      * Удаляет пользователя по ID.
      */
     void deleteById(Long id);

     /**
      * Находит пользователя по email.
      */
     Optional<UserEntity> findByEmail(String email);
}