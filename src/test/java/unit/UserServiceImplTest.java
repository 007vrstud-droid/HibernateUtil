package unit;

import com.example.entity.UserEntity;
import com.example.exception.DuplicateResourceException;
import com.example.exception.InvalidDataException;
import com.example.exception.NotFoundException;
import com.example.repository.UserDao;
import com.example.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserDao userDao;
    private UserServiceImpl userService;

    @BeforeEach
    void setup() {
        userDao = mock(UserDao.class);
        userService = new UserServiceImpl(userDao);
    }

    // === CREATE USER ===

    @Test
    void createUser_nullUser_throwsException() {
        assertThrows(InvalidDataException.class, () -> userService.createUser(null));
        verify(userDao, never()).save(any());
    }

    @Test
    void createUser_invalidEmail_throwsException() {
        UserEntity user = new UserEntity(null, "John", "invalid-email", 30, null);
        assertThrows(InvalidDataException.class, () -> userService.createUser(user));
        verify(userDao, never()).save(any());
    }

    @Test
    void createUser_duplicateEmail_throwsException() {
        when(userDao.existsByEmail("john@example.com")).thenReturn(true);
        UserEntity user = new UserEntity(null, "John", "john@example.com", 30, null);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(user));
        verify(userDao, never()).save(any());
    }

    @Test
    void createUser_validUser_savesUser() {
        UserEntity user = new UserEntity(null, "Bob", "bob@example.com", 40, null);
        when(userDao.existsByEmail("bob@example.com")).thenReturn(false);

        userService.createUser(user);

        verify(userDao, times(1)).save(user);
        assertNotNull(user.getCreatedAt());
    }

    // === GET USER ===

    @Test
    void getUserById_invalidId_throwsException() {
        assertThrows(InvalidDataException.class, () -> userService.getUserById(-1L));
        verify(userDao, never()).findById(any());
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userDao.findById(1L)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> userService.getUserById(1L));
    }

//    @Test
//    void getUserById_validId_returnsUser() {
//        UserEntity user = new UserEntity(1L, "Alice", "alice@example.com", 25, LocalDateTime.now());
//        when(userDao.findById(1L)).thenReturn(user);
//
//        UserEntity result = userService.getUserById(1L);
//
//        assertEquals(user, result);
//        verify(userDao).findById(1L);
//    }

    // === GET ALL USERS ===

    @Test
    void getAllUsers_returnsList() {
        List<UserEntity> users = Arrays.asList(
                new UserEntity(1L, "A", "a@ex.com", 20, null),
                new UserEntity(2L, "B", "b@ex.com", 22, null)
        );
        when(userDao.findAll()).thenReturn(users);

        List<UserEntity> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userDao).findAll();
    }

    @Test
    void getAllUsers_emptyList_returnsEmpty() {
        when(userDao.findAll()).thenReturn(Collections.emptyList());
        List<UserEntity> result = userService.getAllUsers();
        assertTrue(result.isEmpty());
    }

    // === UPDATE USER ===

    @Test
    void updateUser_nullUser_throwsException() {
        assertThrows(InvalidDataException.class, () -> userService.updateUser(null));
        verify(userDao, never()).update(any());
    }

    @Test
    void updateUser_notFound_throwsException() {
        UserEntity user = new UserEntity(1L, "John", "john@example.com", 30, null);
        when(userDao.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.updateUser(user));
    }

//    @Test
//    void updateUser_duplicateEmail_throwsException() {
//        // Существующий пользователь с email, который уже есть в базе
//        UserEntity existing = new UserEntity(1L, "Old", "old@example.com", 25, null);
//        // Пользователь, которого мы обновляем, с другим id, но email совпадает с существующим
//        UserEntity update = new UserEntity(2L, "New", "old@example.com", 26, null);
//
//        when(userDao.findById(2L)).thenReturn(update);
//        when(userDao.findByEmail("old@example.com")).thenReturn(existing); // другой пользователь с этим email
//
//        assertThrows(DuplicateResourceException.class, () -> userService.updateUser(update));
//        verify(userDao, never()).update(any());
//    }

//    @Test
//    void updateUser_valid_updatesUser() {
//        UserEntity existing = new UserEntity(1L, "Old", "old@example.com", 25, null);
//        UserEntity update = new UserEntity(1L, "New", "old@example.com", 26, null); // тот же email
//
//        when(userDao.findById(1L)).thenReturn(existing);
//
//        userService.updateUser(update);
//
//        verify(userDao, times(1)).update(update);
//    }

    // === DELETE USER ===

    @Test
    void deleteUser_invalidId_throwsException() {
        assertThrows(InvalidDataException.class, () -> userService.deleteUser(0L));
        verify(userDao, never()).deleteById(any());
    }

    @Test
    void deleteUser_notFound_throwsException() {
        when(userDao.findById(1L)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));
    }

//    @Test
//    void deleteUser_valid_callsDao() {
//        UserEntity existing = new UserEntity(1L, "John", "john@example.com", 30, null);
//        when(userDao.findById(1L)).thenReturn(existing);
//
//        userService.deleteUser(1L);
//
//        verify(userDao).deleteById(1L);
//    }

    // === IS EMAIL EXISTS ===

    @Test
    void isEmailExists_invalidEmail_throwsException() {
        assertThrows(InvalidDataException.class, () -> userService.isEmailExists("invalid"));
    }

    @Test
    void isEmailExists_validEmail_returnsTrue() {
        when(userDao.existsByEmail("test@example.com")).thenReturn(true);
        assertTrue(userService.isEmailExists("test@example.com"));
    }

    @Test
    void isEmailExists_validEmail_returnsFalse() {
        when(userDao.existsByEmail("test@example.com")).thenReturn(false);
        assertFalse(userService.isEmailExists("test@example.com"));
    }
}
