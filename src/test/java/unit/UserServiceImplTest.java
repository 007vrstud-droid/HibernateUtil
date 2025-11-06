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
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserChecks userChecks;
    private UserMapper userMapper;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userChecks = mock(UserChecks.class);
        userMapper = mock(UserMapper.class);
        userService = new UserServiceImpl(userRepository, userChecks, userMapper);

        // Заглушки для валидации
        doNothing().when(userChecks).validateUserNotNull(any());
        doNothing().when(userChecks).validateEmail(anyString());
        doNothing().when(userChecks).validateAge(any(Integer.class));
        doNothing().when(userChecks).validateId(anyLong());
        doNothing().when(userChecks).ensureEmailUniqueForUpdate(any());

        // Заглушка для маппера
        when(userMapper.toResponse(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity e = inv.getArgument(0);
            UserResponse r = new UserResponse();
            r.setId(e.getId());
            r.setName(e.getName());
            r.setEmail(e.getEmail());
            r.setAge(e.getAge());
            return r;
        });
    }

    @Test
    void createUser_shouldSaveUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setAge(25);

        userService.createUser(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Alice");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getAge()).isEqualTo(25);
    }

    @Test
    void updateUser_shouldUpdateExistingUser() {
        Long userId = 1L;

        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(userId); // DTO поле Integer
        request.setName("Bob");
        request.setEmail("bob@example.com");
        request.setAge(30);

        UserEntity existing = new UserEntity();
        existing.setId(userId);
        existing.setName("OldName");
        existing.setEmail("old@example.com");
        existing.setAge(20);

        // Преобразуем Integer в Long перед вызовом репозитория
        when(userRepository.findById(Long.valueOf(request.getId()))).thenReturn(Optional.of(existing));

        userService.updateUser(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity updated = captor.getValue();

        assertThat(updated.getName()).isEqualTo("Bob");
        assertThat(updated.getEmail()).isEqualTo("bob@example.com");
        assertThat(updated.getAge()).isEqualTo(30);
    }

    @Test
    void updateUser_nonExistingUser_shouldThrow() {
        Long userId = 99L;

        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(userId);
        request.setName("NonExist");
        request.setEmail("no@example.com");
        request.setAge(50);

        when(userRepository.findById(Long.valueOf(request.getId()))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID 99 не найден");
    }

    @Test
    void getUserById_existingUser_shouldReturnResponse() {
        Long userId = 1L;

        UserEntity entity = new UserEntity();
        entity.setId(userId);
        entity.setName("Test");
        entity.setEmail("test@example.com");
        entity.setAge(40);

        when(userRepository.findById(userId)).thenReturn(Optional.of(entity));

        Optional<UserResponse> response = userService.getUserById(userId);

        assertThat(response).isPresent();
        assertThat(response.get().getName()).isEqualTo("Test");
        assertThat(response.get().getEmail()).isEqualTo("test@example.com");
        assertThat(response.get().getAge()).isEqualTo(40);
    }

    @Test
    void getUserById_nonExistingUser_shouldReturnEmpty() {
        Long userId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<UserResponse> response = userService.getUserById(userId);

        assertThat(response).isEmpty();
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        UserEntity u1 = new UserEntity();
        u1.setId(1L);
        u1.setName("A");
        u1.setEmail("a@example.com");
        u1.setAge(20);

        UserEntity u2 = new UserEntity();
        u2.setId(2L);
        u2.setName("B");
        u2.setEmail("b@example.com");
        u2.setAge(30);

        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<UserResponse> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getName()).isEqualTo("A");
        assertThat(users.get(1).getName()).isEqualTo("B");
    }

    @Test
    void deleteUser_existingUser_shouldCallDaoDelete() {
        Long userId = 1L;

        UserEntity entity = new UserEntity();
        entity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(entity));

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_nonExistingUser_shouldThrow() {
        Long userId = 99L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID 99 не найден");
    }

    @Test
    void isEmailExists_shouldReturnTrueIfFound() {
        UserEntity entity = new UserEntity();
        entity.setEmail("exist@example.com");

        when(userRepository.findByEmail("exist@example.com")).thenReturn(Optional.of(entity));

        boolean result = userService.isEmailExists("exist@example.com");

        assertThat(result).isTrue();
    }

    @Test
    void isEmailExists_shouldReturnFalseIfNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        boolean result = userService.isEmailExists("notfound@example.com");

        assertThat(result).isFalse();
    }

    @Test
    void isEmailExists_shouldReturnFalseForNullOrBlank() {
        assertThat(userService.isEmailExists(null)).isFalse();
        assertThat(userService.isEmailExists("")).isFalse();
        assertThat(userService.isEmailExists("   ")).isFalse();
    }
}
