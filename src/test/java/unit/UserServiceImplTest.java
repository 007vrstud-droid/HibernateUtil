package unit;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.entity.UserEntity;
import com.example.exception.NotFoundException;
import com.example.mapper.UserMapper;
import com.example.repository.UserRepository;
import com.example.service.UserServiceImpl;
import com.example.util.UserChecks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserChecks userChecks;
    private UserMapper userMapper;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userChecks = Mockito.mock(UserChecks.class);
        userMapper = Mockito.mock(UserMapper.class);
        userService = new UserServiceImpl(userRepository, userChecks, userMapper);

        Mockito.doNothing().when(userChecks).validateUserNotNull(Mockito.any());
        Mockito.doNothing().when(userChecks).validateEmail(Mockito.anyString());
        Mockito.doNothing().when(userChecks).validateAge(Mockito.any());
        Mockito.doNothing().when(userChecks).validateId(Mockito.anyLong());
        Mockito.doNothing().when(userChecks).ensureEmailUniqueForCreate(Mockito.anyString());
        Mockito.doNothing().when(userChecks).ensureEmailUniqueForUpdate(Mockito.any(UserEntity.class));
    }

    @Test
    void createUser_shouldValidateAndSaveUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("User1");
        request.setEmail("user1@example.com");
        request.setAge(25);

        UserEntity mapped = new UserEntity();
        mapped.setName("User1");
        mapped.setEmail("user1@example.com");
        mapped.setAge(25);

        Mockito.when(userMapper.fromCreateRequest(request)).thenReturn(mapped);

        userService.createUser(request);

        Mockito.verify(userChecks).validateUserNotNull(request);
        Mockito.verify(userChecks).validateEmail("user1@example.com");
        Mockito.verify(userChecks).validateAge(25);
        Mockito.verify(userChecks).ensureEmailUniqueForCreate("user1@example.com");

        Mockito.verify(userMapper).fromCreateRequest(request);
        Mockito.verify(userRepository).save(mapped);
    }

    @Test
    void updateUser_shouldUpdateExistingUser() {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(userId);
        request.setName("User2");
        request.setEmail("user2@example.com");
        request.setAge(30);

        UserEntity existing = new UserEntity();
        existing.setId(userId);
        existing.setName("OldName");
        existing.setEmail("old@example.com");
        existing.setAge(20);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existing));

        userService.updateUser(request);

        Mockito.verify(userChecks).validateId(userId);
        Mockito.verify(userChecks).validateUserNotNull(request);
        Mockito.verify(userChecks).validateEmail("user2@example.com");
        Mockito.verify(userChecks).validateAge(30);

        Mockito.verify(userMapper).updateEntityFromDto(request, existing);
        Mockito.verify(userChecks).ensureEmailUniqueForUpdate(existing);
        Mockito.verify(userRepository).save(existing);
    }

    @Test
    void updateUser_nonExistingUser_shouldThrow() {
        Long userId = 99L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(userId);
        request.setName("Ghost");
        request.setEmail("ghost@example.com");

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID 99 не найден");
    }

    @Test
    void getUserById_existingUser_shouldReturnResponse() {
        Long userId = 1L;
        UserEntity entity = new UserEntity();
        entity.setId(userId);
        entity.setName("User1");
        entity.setEmail("user1@example.com");
        entity.setAge(25);

        UserResponse response = new UserResponse();
        response.setId(userId);
        response.setName("User1");
        response.setEmail("user1@example.com");
        response.setAge(25);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(entity));
        Mockito.when(userMapper.toResponse(entity)).thenReturn(response);

        Optional<UserResponse> result = userService.getUserById(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("User1");
        Mockito.verify(userMapper).toResponse(entity);
    }

    @Test
    void getUserById_nonExistingUser_shouldReturnEmpty() {
        Long userId = 2L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<UserResponse> result = userService.getUserById(userId);

        assertThat(result).isEmpty();
        Mockito.verify(userRepository).findById(userId);
    }

    @Test
    void getAllUsers_shouldReturnMappedList() {
        UserEntity e1 = new UserEntity();
        e1.setId(1L);
        e1.setName("User1");

        UserEntity e2 = new UserEntity();
        e2.setId(2L);
        e2.setName("User2");

        UserResponse r1 = new UserResponse();
        r1.setId(1L);
        r1.setName("User1");

        UserResponse r2 = new UserResponse();
        r2.setId(2L);
        r2.setName("User2");

        Mockito.when(userRepository.findAll()).thenReturn(Arrays.asList(e1, e2));
        Mockito.when(userMapper.toResponse(e1)).thenReturn(r1);
        Mockito.when(userMapper.toResponse(e2)).thenReturn(r2);

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("User1");
        assertThat(result.get(1).getName()).isEqualTo("User2");
    }

    @Test
    void deleteUser_existingUser_shouldDeleteById() {
        Long userId = 1L;
        UserEntity entity = new UserEntity();
        entity.setId(userId);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(entity));

        userService.deleteUser(userId);

        Mockito.verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_nonExistingUser_shouldThrow() {
        Long userId = 77L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID 77 не найден");
    }

    @Test
    void isEmailExists_shouldReturnTrueIfFound() {
        String email = "user1@example.com";
        UserEntity entity = new UserEntity();
        entity.setEmail(email);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(entity));

        boolean result = userService.isEmailExists(email);

        assertThat(result).isTrue();
    }

    @Test
    void isEmailExists_shouldReturnFalseIfNotFound() {
        Mockito.when(userRepository.findByEmail("no@example.com")).thenReturn(Optional.empty());

        boolean result = userService.isEmailExists("no@example.com");

        assertThat(result).isFalse();
    }

    @Test
    void isEmailExists_shouldReturnFalseForNullOrBlank() {
        assertThat(userService.isEmailExists(null)).isFalse();
        assertThat(userService.isEmailExists("")).isFalse();
        assertThat(userService.isEmailExists("   ")).isFalse();
    }
}