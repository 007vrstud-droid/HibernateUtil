package com.example.repository;

import com.example.entity.UserEntity;
import com.example.util.HibernateSessionFactoryProvider;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

@Slf4j
public class UserDaoHibernateImpl implements UserDao {

    private final SessionFactory sessionFactory;

    public UserDaoHibernateImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public UserDaoHibernateImpl() {
        this(HibernateSessionFactoryProvider.getSessionFactory());
    }

    @Override
    public void save(UserEntity user) {
        executeInTransaction(session -> session.save(user));
        log.info("Пользователь сохранён: {}", user);
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(UserEntity.class, id));
        } catch (Exception e) {
            log.error("Ошибка при получении пользователя по id {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<UserEntity> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM UserEntity", UserEntity.class).getResultList();
        } catch (Exception e) {
            log.error("Ошибка при получении всех пользователей", e);
            return List.of();
        }
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM UserEntity u WHERE u.email = :email", UserEntity.class)
                    .setParameter("email", email)
                    .uniqueResultOptional();
        } catch (Exception e) {
            log.error("Ошибка при поиске пользователя по email {}", email, e);
            return Optional.empty();
        }
    }

    @Override
    public void update(UserEntity user) {
        executeInTransaction(session -> session.merge(user));
        log.info("Пользователь обновлён: {}", user);
    }

    @Override
    public void deleteById(Long id) {
        executeInTransaction(session -> {
            UserEntity user = session.get(UserEntity.class, id);
            if (user != null) {
                session.delete(user);
                log.info("Пользователь с id {} удалён", id);
            } else {
                log.warn("Пользователь с id {} не найден", id);
            }
        });
    }

    private void executeInTransaction(SessionConsumer consumer) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            consumer.accept(session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception ex) {
                    log.error("Ошибка при откате транзакции", ex);
                }
            }
            log.error("Ошибка в транзакции", e);
        }
    }

    @FunctionalInterface
    private interface SessionConsumer {
        void accept(Session session) throws Exception;
    }
}
