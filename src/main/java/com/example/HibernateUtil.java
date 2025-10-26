package com.example;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public class HibernateUtil {

    /**
     * Единственный экземпляр фабрики сессий Hibernate.
     */
    private static final SessionFactory sessionFactory;

    static {
        try {
            // Загружаем настройки Hibernate из файла hibernate.properties
            Properties properties = new Properties();
            properties.load(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate.properties"));

            // Создаём конфигурацию Hibernate
            Configuration configuration = new Configuration();
            configuration.setProperties(properties);

            // Регистрируем аннотированные классы (Entity)
            configuration.addAnnotatedClass(User.class);

            // Строим фабрику сессий
            sessionFactory = configuration.buildSessionFactory();
        } catch (IOException e) {
            log.error("Ошибка загрузки hibernate.properties: {}", e.getMessage(), e);
            throw new ExceptionInInitializerError(e);
        } catch (Throwable ex) {
            log.error("Ошибка инициализации SessionFactory", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Возвращает глобальный экземпляр {@link SessionFactory}, используемый для открытия сессий.
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
