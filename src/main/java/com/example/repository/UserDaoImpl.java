package com.example.repository;

import com.example.model.UserEntity;
import com.example.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

@Slf4j
public class UserDaoImpl implements UserDao {
    private final SessionFactory sessionFactory;

    public UserDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public UserDaoImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    /**
     * Сохраняет нового пользователя в базе данных.
     */

    public void saveUser(UserEntity user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
            log.info("Пользователь сохранён: {}", user);
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Ошибка при сохранении пользователя", e);
        }
    }

    /**
     * Извлекает пользователя из базы данных по его идентификатору.
     */
    public UserEntity getUser(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(UserEntity.class, id);
        } catch (Exception e) {
            log.error("Ошибка при получении пользователя по id {}", id, e);
            return null;
        }
    }

    /**
     * Извлекает всех пользователей из базы данных.
     */
    public List<UserEntity> getAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM UserEntity", UserEntity.class).getResultList();
        } catch (Exception e) {
            log.error("Ошибка при получении всех пользователей", e);
            return List.of();
        }
    }

    /**
     * Обновляет данные существующего пользователя в базе данных.
     */
    public void updateUser(UserEntity user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(user); // merge — безопаснее для detached объектов
            transaction.commit();
            log.info("Пользователь обновлён: {}", user);
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Ошибка при обновлении пользователя", e);
        }
    }

    /**
     * Удаляет пользователя из базы данных по идентификатору.
     */
    public void deleteUser(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserEntity user = session.get(UserEntity.class, id);
            if (user != null) {
                session.delete(user);
                transaction.commit();
                log.info("Пользователь с id {} удалён", id);
            } else {
                log.warn("Пользователь с id {} не найден", id);
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Ошибка при удалении пользователя", e);
        }
    }


    public void deleteAllUsers() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM UserEntity").executeUpdate();
            transaction.commit();
            log.info("Все пользователи удалены");
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Ошибка при удалении всех пользователей", e);
        }
    }

    /**
     * Проверяет, существует ли пользователь с указанным адресом электронной почты.
     */
    public boolean isEmailExists(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("select count(u.id) from UserEntity u where u.email = :email", Long.class)
                    .setParameter("email", email)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Ошибка при проверке email: {}", email, e);
            return false;
        }
    }
}
