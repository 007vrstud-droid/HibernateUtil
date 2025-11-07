package unit;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.entity.UserEntity;
import com.example.exception.NotFoundException;
import com.example.repository.UserDao;
import com.example.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    private UserDao userDao;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        userService = new UserServiceImpl(userDao);
    }

    // ================= CREATE =================
    @Test
    void createUser_shouldSaveUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setAge(25);

        // эмулируем ensureEmailUniqueForCreate (внутри UserChecks)
        doNothing().when(userDao).save(any(UserEntity.class));

        userService.createUser(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userDao).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo(request.getName());
        assertThat(saved.getEmail()).isEqualTo(request.getEmail());
        assertThat(saved.getAge()).isEqualTo(request.getAge());
    }

    // ================= UPDATE =================
    @Test
    void updateUser_shouldUpdateExistingUser() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(1L);
        request.setName("Bob");
        request.setEmail("bob@example.com");
        request.setAge(30);

        UserEntity existing = new UserEntity();
        existing.setId(1L);
        existing.setName("OldName");
        existing.setEmail("old@example.com");
        existing.setAge(20);

        when(userDao.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(userDao).update(any(UserEntity.class));

        userService.updateUser(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userDao).update(captor.capture());
        UserEntity updated = captor.getValue();

        assertThat(updated.getName()).isEqualTo(request.getName());
        assertThat(updated.getEmail()).isEqualTo(request.getEmail());
        assertThat(updated.getAge()).isEqualTo(request.getAge());
    }

    @Test
    void updateUser_nonExistingUser_shouldThrow() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(99L);
        request.setName("NonExist");
        request.setEmail("no@example.com");
        request.setAge(50);

        when(userDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID 99 не найден");
    }

    // ================= GET BY ID =================
    @Test
    void getUserById_existingUser_shouldReturnResponse() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setName("Test");
        entity.setEmail("test@example.com");
        entity.setAge(40);

        when(userDao.findById(1L)).thenReturn(Optional.of(entity));

        Optional<UserResponse> response = userService.getUserById(1L);

        assertThat(response).isPresent();
        assertThat(response.get().getName()).isEqualTo(entity.getName());
        assertThat(response.get().getEmail()).isEqualTo(entity.getEmail());
        assertThat(response.get().getAge()).isEqualTo(entity.getAge());

    }

    @Test
    void getUserById_nonExistingUser_shouldReturnEmpty() {
        when(userDao.findById(2L)).thenReturn(Optional.empty());

        Optional<UserResponse> response = userService.getUserById(2L);

        assertThat(response).isEmpty();
    }

    // ================= GET ALL =================
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

        when(userDao.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<UserResponse> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getName()).isEqualTo("A");
        assertThat(users.get(1).getName()).isEqualTo("B");
    }

    // ================= DELETE =================
    @Test
    void deleteUser_existingUser_shouldCallDaoDelete() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);

        when(userDao.findById(1L)).thenReturn(Optional.of(entity));

        userService.deleteUser(1L);

        verify(userDao).deleteById(1L);
    }

    @Test
    void deleteUser_nonExistingUser_shouldThrow() {
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID 99 не найден");
    }

    // ================= EMAIL EXIST =================
    @Test
    void isEmailExists_shouldReturnTrueIfFound() {
        UserEntity entity = new UserEntity();
        entity.setEmail("exist@example.com");

        when(userDao.findByEmail("exist@example.com")).thenReturn(Optional.of(entity));

        boolean result = userService.isEmailExists("exist@example.com");

        assertThat(result).isTrue();
    }

    @Test
    void isEmailExists_shouldReturnFalseIfNotFound() {
        when(userDao.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        boolean result = userService.isEmailExists("notfound@example.com");

        assertThat(result).isFalse();
    }

    @Test
    void isEmailExists_shouldReturnFalseForNullEmail() {
        boolean result = userService.isEmailExists(null);
        assertThat(result).isFalse();
    }

    @Test
    void isEmailExists_shouldReturnFalseForBlankEmail() {
        boolean result1 = userService.isEmailExists("");
        boolean result2 = userService.isEmailExists("   ");

        assertThat(result1).isFalse();
        assertThat(result2).isFalse();
    }

}
