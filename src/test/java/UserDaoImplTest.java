import com.example.model.UserEntity;
import com.example.repository.UserDaoImpl;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class UserDaoImplTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("admin");

    private SessionFactory sessionFactory;
    private UserDaoImpl userDao;

    @BeforeEach
    void setup() {
        // Настройка Hibernate
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", postgreSQLContainer.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgreSQLContainer.getUsername());
        configuration.setProperty("hibernate.connection.password", postgreSQLContainer.getPassword());
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.addAnnotatedClass(UserEntity.class);

        sessionFactory = configuration.buildSessionFactory();

        // Создаем DAO
        userDao = new UserDaoImpl(sessionFactory);

        // Очистка базы
        userDao.deleteAllUsers();
    }

    @AfterEach
    void teardown() {
        if (sessionFactory != null) sessionFactory.close();
    }

    @Test
    void saveUser_andGetUserById_success() {
        UserEntity user = new UserEntity(null, "John", "john@example.com", 30, LocalDateTime.now());
        userDao.saveUser(user);

        assertNotNull(user.getId());

        UserEntity fetched = userDao.getUser(user.getId());

        assertEquals(user.getId(), fetched.getId());
        assertEquals(user.getName(), fetched.getName());
        assertEquals(user.getEmail(), fetched.getEmail());
        assertEquals(user.getAge(), fetched.getAge());
        long secondsDiff = Math.abs(user.getCreatedAt().until(fetched.getCreatedAt(), ChronoUnit.SECONDS));
        assertTrue(secondsDiff < 1, "Дата создания отличается менее чем на 1 секунду");
    }

    @Test
    void updateUser_success() {
        UserEntity user = new UserEntity(null, "Alice", "alice@example.com", 25, LocalDateTime.now());
        userDao.saveUser(user);

        user.setName("Alice Updated");
        userDao.updateUser(user);

        UserEntity fetched = userDao.getUser(user.getId());
        assertEquals("Alice Updated", fetched.getName());
    }

    @Test
    void deleteUser_success() {
        UserEntity user = new UserEntity(null, "Bob", "bob@example.com", 40, LocalDateTime.now());
        userDao.saveUser(user);

        userDao.deleteUser(user.getId());

        assertNull(userDao.getUser(user.getId()));
    }

    @Test
    void isEmailExists_success() {
        UserEntity user = new UserEntity(null, "Charlie", "charlie@example.com", 35, LocalDateTime.now());
        userDao.saveUser(user);

        assertTrue(userDao.isEmailExists("charlie@example.com"));
        assertFalse(userDao.isEmailExists("nonexistent@example.com"));
    }


    @Test
    void getAllUsers_returnsAll() {
        String email1 = "user1_" + UUID.randomUUID() + "@example.com";
        String email2 = "user2_" + UUID.randomUUID() + "@example.com";

        userDao.saveUser(new UserEntity(null, "User1", email1, 20, LocalDateTime.now()));
        userDao.saveUser(new UserEntity(null, "User2", email2, 22, LocalDateTime.now()));

        List<UserEntity> users = userDao.getAllUsers();
        assertEquals(2, users.size());
    }
}
