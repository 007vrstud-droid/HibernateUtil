package com.example.controller;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse user1;
    private UserResponse user2;

    @BeforeEach
    void setUp() {
        user1 = new UserResponse();
        user1.setId(1L);
        user1.setName("User1");
        user1.setEmail("user1@example.com");
        user1.setAge(25);

        user2 = new UserResponse();
        user2.setId(2L);
        user2.setName("User2");
        user2.setEmail("user2@example.com");
        user2.setAge(30);
    }

    @Test
    @DisplayName("Создание пользователя")
    void createUser_shouldReturnOk() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("User1");
        request.setEmail("user1@example.com");
        request.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    @DisplayName("Получение существующего пользователя")
    void getUser_existingUser_shouldReturnUser() throws Exception {
        Mockito.when(userService.getUserById(1L)).thenReturn(Optional.of(user1));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("User1"))
                .andExpect(jsonPath("$.email").value("user1@example.com"))
                .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    @DisplayName("Получение несуществующего пользователя")
    void getUser_nonExistingUser_shouldReturnNotFound() throws Exception {
        Mockito.when(userService.getUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateUser_shouldReturnOk() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("User2");
        request.setEmail("user2@example.com");
        request.setAge(30);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(userService).updateUser(any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void getAllUsers_shouldReturnList() throws Exception {
        Mockito.when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("User1"))
                .andExpect(jsonPath("$[1].name").value("User2"));
    }
}
