import com.example.model.UserEntity;
import com.example.repository.UserDao;
import com.example.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    private UserDao userDao;
    private UserServiceImpl userService;

    @BeforeEach
    void setup() {
        userDao = mock(UserDao.class);
        userService = new UserServiceImpl(userDao);
    }

    @Test
    void createUser_nullUser_doesNotSave() {
        userService.createUser(null);
        verify(userDao, times(0)).saveUser(any());

    }

    @Test
    void createUser_nullEmail_doesNotSave() {
        UserEntity user = new UserEntity(null, "John", null, 30, null);
        userService.createUser(user);
        verify(userDao, never()).saveUser(any());
    }

    @Test
    void createUser_emailWithoutAt_doesNotSave() {
        UserEntity user = new UserEntity(null, "John", "invalid-email", 30, null);
        userService.createUser(user);
        verify(userDao, never()).saveUser(any());
    }

    @Test
    void createUser_duplicateEmail_doesNotSave() {
        UserEntity user = new UserEntity(null, "John", "john@example.com", 30, null);
        when(userDao.isEmailExists("john@example.com")).thenReturn(true);

        userService.createUser(user);

        verify(userDao, never()).saveUser(any());
    }

    @Test
    void createUser_validUser_savesUser() {
        UserEntity user = new UserEntity(null, "Bob", "bob@example.com", 40, null);
        when(userDao.isEmailExists("bob@example.com")).thenReturn(false);

        userService.createUser(user);

        verify(userDao, times(1)).saveUser(user);
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void getUserById_nullId_returnsNull() {
        assertNull(userService.getUserById(null));
        verify(userDao, never()).getUser(any());
    }

    @Test
    void getUserById_negativeId_returnsNull() {
        assertNull(userService.getUserById(-1L));
        verify(userDao, never()).getUser(any());
    }

    @Test
    void getUserById_validId_returnsUser() {
        UserEntity expectedUser = new UserEntity(1L, "John", "john@example.com", 30, null);
        when(userDao.getUser(1L)).thenReturn(
                new UserEntity(1L, "John", "john@example.com", 30, null)
        );

        UserEntity actual = userService.getUserById(1L);

        assertNotNull(actual);
        assertEquals(expectedUser, actual); // <- теперь тест упадет
        verify(userDao, times(1)).getUser(1L);
    }

    @Test
    void testGetAllUsers_Negative() {
        when(userDao.getAllUsers()).thenThrow(new RuntimeException("DB error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getAllUsers();
        });

        assertEquals("DB error", exception.getMessage());
    }

    @Test
    void testGetAllUsers_Positive() {
        UserEntity user1 = new UserEntity(1L, "Alice", "alice@example.com", 25, null);
        UserEntity user2 = new UserEntity(2L, "Bob", "bob@example.com", 30, null);

        when(userDao.getAllUsers()).thenReturn(
                Arrays.asList(
                        new UserEntity(user1.getId(), user1.getName(), user1.getEmail(), user1.getAge(), user1.getCreatedAt()),
                        new UserEntity(user2.getId(), user2.getName(), user2.getEmail(), user2.getAge(), user2.getCreatedAt())
                )
        );

        List<UserEntity> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertNotSame(user1, result.get(0));
        assertNotSame(user2, result.get(1));
        assertEquals(user1, result.get(0));
        assertEquals(user2, result.get(1));
        verify(userDao, times(1)).getAllUsers();
    }

    @Test
    void testGetAllUsers_EmptyList() {
        when(userDao.getAllUsers()).thenReturn(Collections.emptyList());

        List<UserEntity> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateUser_nullUser_doesNotCallDao() {
        userService.updateUser(null);
        verify(userDao, never()).updateUser(any());
    }

    @Test
    void updateUser_nullId_doesNotCallDao() {
        UserEntity user = new UserEntity(null, "John", "john@example.com", 30, null);
        userService.updateUser(user);
        verify(userDao, never()).updateUser(any());
    }

    @Test
    void updateUser_nonExistingUser_doesNotCallDao() {
        UserEntity user = new UserEntity(1L, "John", "john@example.com", 30, null);
        when(userDao.getUser(1L)).thenReturn(null);

        userService.updateUser(user);
        verify(userDao, never()).updateUser(any());
    }


    @Test
    void updateUser_nonExistingUser_doesNotUpdate() {
        UserEntity user = new UserEntity(1L, "Bob", "bob@example.com", 40, LocalDateTime.now());
        when(userDao.getUser(1L)).thenReturn(null);

        userService.updateUser(user);

        verify(userDao, never()).updateUser(any());
    }

    @Test
    void updateUser_duplicateEmail_doesNotUpdate() {
        UserEntity existing = new UserEntity(1L, "John", "john@example.com", 30, null);
        UserEntity update = new UserEntity(1L, "John", "alice@example.com", 30, null);

        when(userDao.getUser(1L)).thenReturn(existing);          // возвращаем существующего пользователя
        when(userDao.isEmailExists("alice@example.com")).thenReturn(true); // новый email уже занят

        userService.updateUser(update);

        verify(userDao, never()).updateUser(any());
    }

    @Test
    void updateUser_validUser_updatesUser() {
        UserEntity existing = new UserEntity(1L, "John", "john@example.com", 30, null);
        UserEntity update = new UserEntity(1L, "John Updated", "john@example.com", 31, null);

        when(userDao.getUser(1L)).thenReturn(existing);

        userService.updateUser(update);

        verify(userDao, times(1)).updateUser(update);
    }

    @Test
    void deleteUser_nullId_doesNotCallDao() {
        userService.deleteUser(null);
        verify(userDao, never()).deleteUser(any());
    }

    @Test
    void deleteUser_negativeId_doesNotCallDao() {
        userService.deleteUser(-1L);
        verify(userDao, never()).deleteUser(any());
    }

    @Test
    void deleteUser_validId_callsDao() {
        Long id = 1L;
        userService.deleteUser(id);
        verify(userDao, times(1)).deleteUser(id);
    }

    @Test
    void isEmailExists_emailExists_returnsTrue() {
        String email = "test@example.com";
        when(userDao.isEmailExists(email)).thenReturn(true);

        boolean result = userService.isEmailExists(email);

        assertTrue(result);
        verify(userDao, times(1)).isEmailExists(email);
    }

    @Test
    void isEmailExists_emailDoesNotExist_returnsFalse() {
        String email = "test@example.com";
        when(userDao.isEmailExists(email)).thenReturn(false);

        boolean result = userService.isEmailExists(email);

        assertFalse(result);
        verify(userDao, times(1)).isEmailExists(email);
    }

}
