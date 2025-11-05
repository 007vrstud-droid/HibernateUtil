package com.example.mapper;

//import com.example.dto.UserResponse;
import com.example.dto.UserResponse;
import com.example.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(UserEntity entity);
}