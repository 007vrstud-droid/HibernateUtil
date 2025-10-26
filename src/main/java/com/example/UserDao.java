package com.example;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Slf4j
public class UserDao {

    /**
     * Сохраняет нового пользователя в базе данных.
     */
    public void saveUser(User user) {
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
    public User getUser(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            log.error("Ошибка при получении пользователя по id {}", id, e);
            return null;
        }
    }

    /**
     * Обновляет данные существующего пользователя в базе данных.
     */
    public void updateUser(User user) {
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
            User user = session.get(User.class, id);
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

    /**
     * Проверяет, существует ли пользователь с указанным адресом электронной почты.
     */
    public boolean isEmailExists(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select count(u.id) from User u where u.email = :email";
            Long count = session.createQuery(hql, Long.class)
                    .setParameter("email", email)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Ошибка при проверке email: {}", email, e);
            return false;
        }
    }
}
