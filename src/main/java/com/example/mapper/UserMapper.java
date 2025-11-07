package com.example.mapper;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(UserEntity entity);
    UserEntity fromCreateRequest(UserCreateRequest request);
    void updateEntityFromDto(UserUpdateRequest dto, @MappingTarget UserEntity entity);
}