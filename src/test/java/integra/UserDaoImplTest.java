package integra;

import com.example.entity.UserEntity;
import com.example.repository.UserDaoHibernateImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDaoImplTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("admin");

    private SessionFactory sessionFactory;
    private UserDaoHibernateImpl userDao;

    @BeforeAll
    void beforeAll() {
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", postgreSQLContainer.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgreSQLContainer.getUsername());
        configuration.setProperty("hibernate.connection.password", postgreSQLContainer.getPassword());
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");
        configuration.setProperty("hibernate.current_session_context_class", "thread");
        configuration.addAnnotatedClass(UserEntity.class);

        sessionFactory = configuration.buildSessionFactory();
        userDao = new UserDaoHibernateImpl(sessionFactory);
    }

    @BeforeEach
    void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from UserEntity").executeUpdate();
            tx.commit();
        }
    }

    @AfterAll
    void teardown() {
        if (sessionFactory != null) sessionFactory.close();
    }

    @Test
    void saveUser_andGetUserById_success() {
        UserEntity user = new UserEntity(null, "John", "john@example.com", 30, LocalDateTime.now());
        userDao.save(user);

        assertNotNull(user.getId());

        UserEntity fetched = userDao.findById(user.getId());

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
        userDao.save(user);

        user.setName("Alice Updated");
        userDao.update(user);

        UserEntity fetched = userDao.findById(user.getId());
        assertEquals("Alice Updated", fetched.getName());
    }

    @Test
    void deleteUser_success() {
        UserEntity user = new UserEntity(null, "Bob", "bob@example.com", 40, LocalDateTime.now());
        userDao.save(user);

        userDao.deleteById(user.getId());

        assertNull(userDao.findById(user.getId()));
    }

    @Test
    void isEmailExists_success() {
        UserEntity user = new UserEntity(null, "Charlie", "charlie@example.com", 35, LocalDateTime.now());
        userDao.save(user);

        assertTrue(userDao.existsByEmail("charlie@example.com"));
        assertFalse(userDao.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void getAllUsers_returnsAll() {
        String email1 = "user1_" + UUID.randomUUID() + "@example.com";
        String email2 = "user2_" + UUID.randomUUID() + "@example.com";

        userDao.save(new UserEntity(null, "User1", email1, 20, LocalDateTime.now()));
        userDao.save(new UserEntity(null, "User2", email2, 22, LocalDateTime.now()));

        List<UserEntity> users = userDao.findAll();
        assertEquals(2, users.size());
    }
}
