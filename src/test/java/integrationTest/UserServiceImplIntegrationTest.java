//package integrationTest;
//
//import com.example.dto.UserCreateRequest;
//import com.example.dto.UserUpdateRequest;
//import com.example.dto.UserResponse;
//import com.example.entity.UserEntity;
//import com.example.exception.NotFoundException;
//import com.example.repository.UserRepository;
//import com.example.service.UserServiceImpl;
//import com.example.util.UserChecks;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//@Transactional
//class UserServiceImplIntegrationTest {
//
//    @Autowired
//    private UserServiceImpl userService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private UserChecks userChecks;
//
//    private UserEntity savedUser;
//
//    @BeforeEach
//    void setUp() {
//        UserEntity user = new UserEntity();
//        user.setName("Test User");
//        user.setEmail("testuser@example.com");
//        user.setAge(30);
//
//        savedUser = userRepository.save(user);
//    }
//
//    @Test
//    void createUser_shouldPersistUserInDatabase() {
//        // Arrange
//        UserCreateRequest createRequest = new UserCreateRequest();
//        createRequest.setName("Alice");
//        createRequest.setEmail("alice@example.com");
//        createRequest.setAge(25);
//
//        // Act
//        userService.createUser(createRequest);
//
//        // Assert
//        UserEntity savedEntity = userRepository.findById(savedUser.getId()).orElseThrow();
//        assertThat(savedEntity).isNotNull();
//        assertThat(savedEntity.getName()).isEqualTo("Alice");
//        assertThat(savedEntity.getEmail()).isEqualTo("alice@example.com");
//        assertThat(savedEntity.getAge()).isEqualTo(25);
//    }
//
//    @Test
//    void updateUser_shouldUpdateUserInDatabase() {
//        // Arrange
//        UserUpdateRequest updateRequest = new UserUpdateRequest();
//        updateRequest.setId(savedUser.getId());
//        updateRequest.setName("Updated Name");
//        updateRequest.setEmail("updatedemail@example.com");
//        updateRequest.setAge(35);
//
//        // Act
//        userService.updateUser(updateRequest);
//
//        // Assert
//        UserEntity updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
//        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
//        assertThat(updatedUser.getEmail()).isEqualTo("updatedemail@example.com");
//        assertThat(updatedUser.getAge()).isEqualTo(35);
//    }
//
//    @Test
//    void getUserById_shouldReturnCorrectUser() {
//        // Act
//        UserResponse userResponse = userService.getUserById(savedUser.getId()).orElseThrow();
//
//        // Assert
//        assertThat(userResponse).isNotNull();
//        assertThat(userResponse.getName()).isEqualTo("Test User");
//        assertThat(userResponse.getEmail()).isEqualTo("testuser@example.com");
//        assertThat(userResponse.getAge()).isEqualTo(30);
//    }
//
//    @Test
//    void deleteUser_shouldRemoveUserFromDatabase() {
//        // Act
//        userService.deleteUser(savedUser.getId());
//
//        // Assert
//        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
//    }
//
//    @Test
//    void getAllUsers_shouldReturnAllUsers() {
//        // Arrange
//        UserEntity anotherUser = new UserEntity();
//        anotherUser.setName("Another User");
//        anotherUser.setEmail("anotheruser@example.com");
//        anotherUser.setAge(40);
//        userRepository.save(anotherUser);
//
//        // Act
//        List<UserResponse> users = userService.getAllUsers();
//
//        // Assert
//        assertThat(users).hasSize(2);
//        assertThat(users).anyMatch(user -> user.getName().equals("Test User"));
//        assertThat(users).anyMatch(user -> user.getName().equals("Another User"));
//    }
//
//    @Test
//    void isEmailExists_shouldReturnTrueIfEmailExists() {
//        // Act
//        boolean result = userService.isEmailExists(savedUser.getEmail());
//
//        // Assert
//        assertThat(result).isTrue();
//    }
//
//    @Test
//    void isEmailExists_shouldReturnFalseIfEmailDoesNotExist() {
//        // Act
//        boolean result = userService.isEmailExists("nonexistent@example.com");
//
//        // Assert
//        assertThat(result).isFalse();
//    }
//
//    @Test
//    void updateUser_nonExistingUser_shouldThrowNotFoundException() {
//        // Arrange
//        UserUpdateRequest updateRequest = new UserUpdateRequest();
//        updateRequest.setId(999L);  // Не существующий ID
//        updateRequest.setName("Non Existing");
//        updateRequest.setEmail("nonexisting@example.com");
//        updateRequest.setAge(50);
//
//        // Act & Assert
//        assertThatThrownBy(() -> userService.updateUser(updateRequest))
//                .isInstanceOf(NotFoundException.class)
//                .hasMessageContaining("Пользователь с ID 999 не найден");
//    }
//}
