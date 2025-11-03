package integrationTest;

import com.example.Main;
import com.example.dto.UserCreateRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.entity.UserEntity;
import com.example.exception.DuplicateResourceException;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = Main.class)
@Testcontainers
@TestMethodOrder(MethodOrderer.DisplayName.class)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void registerPgProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void cleanBefore() {
        userRepository.deleteAll();
    }

    private UserEntity createEntity(String name, String email, int age) {
        UserEntity e = new UserEntity();
        e.setName(name);
        e.setEmail(email);
        e.setAge(age);
        return userRepository.save(e);
    }

    private UserCreateRequest createRequest(String name, String email, int age) {
        UserCreateRequest r = new UserCreateRequest();
        r.setName(name);
        r.setEmail(email);
        r.setAge(age);
        return r;
    }

    private UserUpdateRequest updateRequest(Long id, String name, String email, int age) {
        UserUpdateRequest r = new UserUpdateRequest();
        r.setId(id);
        r.setName(name);
        r.setEmail(email);
        r.setAge(age);
        return r;
    }

    @Test
    @DisplayName("Создание пользователя")
    void createUser_shouldSaveUserToDatabase() {
        var request = createRequest("user1", "user1@example.com", 30);

        userService.createUser(request);

        var all = userRepository.findAll();
        assertThat(all).hasSize(1);

        var saved = all.get(0);
        assertThat(saved)
                .extracting(UserEntity::getName, UserEntity::getEmail, UserEntity::getAge)
                .containsExactly("user1", "user1@example.com", 30);
    }

    @Test
    @DisplayName("Получение списка пользователей")
    void getAllUsers_shouldReturnExistingUsers() {
        createEntity("user1", "user1@example.com", 25);
        createEntity("user2", "user2@example.com", 40);

        var users = userService.getAllUsers();

        assertThat(users)
                .hasSize(2)
                .extracting(UserResponse::getName)
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    @DisplayName("Ошибка при создании пользователя с существующим email")
    void createUser_shouldThrowIfEmailExists() {
        createEntity("user1", "user1@example.com", 25);

        assertThatThrownBy(() ->
                userService.createUser(createRequest("user2", "user1@example.com", 33))
        )
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateUser() {
        var saved = createEntity("user1", "user1@example.com", 25);

        userService.updateUser(updateRequest(saved.getId(), "user1_updated", "user1.updated@example.com", 26));

        var updated = userService.getUserById(saved.getId()).orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("user1.updated@example.com");
        assertThat(updated.getAge()).isEqualTo(26);
    }

    @Test
    @DisplayName("Ошибка при обновлении, если email занят другим пользователем")
    void updateUser_shouldThrowIfEmailUsedByAnother() {
        var u1 = createEntity("user1", "user1@example.com", 25);
        var u2 = createEntity("user2", "user2@example.com", 30);

        var req = updateRequest(u2.getId(), "user2_updated", "user1@example.com", 31);

        assertThatThrownBy(() -> userService.updateUser(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("уже используется");
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser() {
        var entity = createEntity("user1", "user1@example.com", 40);

        userService.deleteUser(entity.getId());

        assertThat(userService.getUserById(entity.getId())).isEmpty();
    }

    @Test
    @DisplayName("Проверка существования email")
    void isEmailExists() {
        createEntity("user1", "user1@example.com", 35);

        assertThat(userService.isEmailExists("user1@example.com")).isTrue();
        assertThat(userService.isEmailExists("nonexistent@example.com")).isFalse();
    }
}