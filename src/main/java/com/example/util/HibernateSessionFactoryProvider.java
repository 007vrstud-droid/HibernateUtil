package com.example.util;

import com.example.entity.UserEntity;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

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

            if (input == null) {
                throw new RuntimeException("Файл hibernate.properties не найден");
            }

            Properties properties = new Properties();
            properties.load(input);

            // Подставляем переменные окружения вида ${VAR} в свойства
            substituteEnvVariables(properties);

            sessionFactory = new Configuration()
                    .setProperties(properties)
                    .addAnnotatedClass(UserEntity.class)
                    .buildSessionFactory();

            log.info("Hibernate SessionFactory успешно создан");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании SessionFactory", e);
        }
    }

    /**
     * Заменяет все вхождения ${VAR} в свойствах на значения соответствующих переменных окружения.
     * Если переменная окружения не найдена, подставляется пустая строка.
     */
    private static void substituteEnvVariables(Properties properties) {
        properties.forEach((key, value) -> {
            String val = value.toString();
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < val.length()) {
                int start = val.indexOf("${", i);
                if (start == -1) {
                    sb.append(val.substring(i));
                    break;
                }
                sb.append(val, i, start);
                int end = val.indexOf("}", start);
                if (end == -1) {
                    sb.append(val.substring(start));
                    break;
                }
                String envKey = val.substring(start + 2, end);
                String envValue = System.getenv(envKey);
                if (envValue != null) {
                    sb.append(envValue);
                    log.info("Подставлено '{}' из переменной окружения '{}'", envValue, envKey);
                } else {
                    log.warn("Переменная окружения '{}' не найдена, подставлено пустое значение", envKey);
                }
                i = end + 1;
            }
            properties.setProperty(key.toString(), sb.toString());
        });
    }
}
