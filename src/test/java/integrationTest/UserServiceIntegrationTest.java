package integrationTest;

import com.example.Main;
import com.example.dto.UserCreateRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.entity.UserEntity;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Main.class) // <-- указываем основной класс
@Testcontainers
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void testCreateAndGetUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setAge(30);

        userService.createUser(request);

        List<UserResponse> users = userService.getAllUsers();
        assertThat(users).hasSize(1);
        UserResponse user = users.get(0);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getAge()).isEqualTo(30);
    }

    @Test
    void testUpdateUser() {
        UserEntity entity = new UserEntity();
        entity.setName("Alice");
        entity.setEmail("alice@example.com");
        entity.setAge(25);
        entity = userRepository.save(entity);

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setId(entity.getId());
        updateRequest.setName("Alice Updated");
        updateRequest.setEmail("alice.updated@example.com");
        updateRequest.setAge(26);

        userService.updateUser(updateRequest);

        Optional<UserResponse> updated = userService.getUserById(entity.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Alice Updated");
        assertThat(updated.get().getEmail()).isEqualTo("alice.updated@example.com");
        assertThat(updated.get().getAge()).isEqualTo(26);
    }

    @Test
    void testDeleteUser() {
        UserEntity entity = new UserEntity();
        entity.setName("Bob");
        entity.setEmail("bob@example.com");
        entity.setAge(40);
        entity = userRepository.save(entity);

        userService.deleteUser(entity.getId());

        Optional<UserResponse> deleted = userService.getUserById(entity.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    void testIsEmailExists() {
        UserEntity entity = new UserEntity();
        entity.setName("Charlie");
        entity.setEmail("charlie@example.com");
        entity.setAge(35);
        userRepository.save(entity);

        assertThat(userService.isEmailExists("charlie@example.com")).isTrue();
        assertThat(userService.isEmailExists("nonexistent@example.com")).isFalse();
    }
}
