package com.example.util;

import com.example.entity.UserEntity;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class HibernateSessionFactoryProvider {

    /**
     * Единственный экземпляр фабрики сессий Hibernate.
     */
    @Getter
    private static final SessionFactory sessionFactory;

    static {
        try (InputStream input = HibernateSessionFactoryProvider.class
                .getClassLoader()
                .getResourceAsStream("hibernate.properties")) {

            if (input == null) throw new RuntimeException("Файл hibernate.properties не найден");

            Properties properties = new Properties();
            properties.load(input);

            sessionFactory = new Configuration()
                    .setProperties(properties)
                    .addAnnotatedClass(UserEntity.class)
                    .buildSessionFactory();

            log.info("Hibernate SessionFactory успешно создан");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании SessionFactory", e);
        }
    }

}
