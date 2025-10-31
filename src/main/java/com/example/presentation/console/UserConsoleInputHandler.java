package com.example.presentation.console;

import com.example.entity.UserEntity;
import com.example.service.UserService;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class UserConsoleInputHandler {

    private final Scanner scanner;
    private static final ValidatorFactory factory =
            Validation.byDefaultProvider()
                    .configure()
                    .messageInterpolator(new ParameterMessageInterpolator())
                    .buildValidatorFactory();
    private static final Validator validator = factory.getValidator();

    public UserConsoleInputHandler(Scanner scanner) {
        this.scanner = scanner;
    }

    // ===== CREATE =====
    public void createUser(UserService userService) {
        UserEntity user = new UserEntity();

        // Шаговая валидация
        user.setName(promptField("Имя", "name", user));
        user.setEmail(promptField("Email", "email", user));
        user.setAge(Integer.parseInt(promptField("Возраст", "age", user)));

        userService.createUser(user);
        System.out.println("Пользователь успешно создан: " + user);
    }

    // ===== READ =====
    public void readUser(UserService userService) {
        System.out.print("Введите ID пользователя для просмотра: ");
        Long id = readLong();

        Optional<UserEntity> userOpt = userService.getUserById(id);
        if (userOpt.isPresent()) {
            System.out.println("Пользователь найден: " + userOpt.get());
        } else {
            System.out.println("Пользователь с ID " + id + " не найден.");
        }
    }

    // ===== UPDATE =====
    public void updateUser(UserService userService) {
        System.out.print("Введите ID пользователя для обновления: ");
        Long id = readLong();

        Optional<UserEntity> userOpt = userService.getUserById(id);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            System.out.println("Текущие данные пользователя: " + user);

            // Шаговая валидация при обновлении
            user.setName(promptField("Имя", "name", user));
            user.setEmail(promptField("Email", "email", user));
            user.setAge(Integer.parseInt(promptField("Возраст", "age", user)));

            userService.updateUser(user);
            System.out.println("Пользователь обновлён: " + user);
        } else {
            System.out.println("Пользователь с ID " + id + " не найден.");
        }
    }

    // ===== DELETE =====
    public void deleteUser(UserService userService) {
        System.out.print("Введите ID пользователя для удаления: ");
        Long id = readLong();

        Optional<UserEntity> userOpt = userService.getUserById(id);
        if (userOpt.isPresent()) {
            userService.deleteUser(id);
            System.out.println("Пользователь с ID " + id + " удалён.");
        } else {
            System.out.println("Пользователь с ID " + id + " не найден.");
        }
    }

    // ===== HELPERS =====
    private Long readLong() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.print("Некорректный ввод. Введите число: ");
            }
        }
    }


    // ===== GET ALL USERS =====
    public void getAllUsers(UserService userService) {
        List<UserEntity> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Пользователи отсутствуют.");
        } else {
            System.out.println("=== Список всех пользователей ===");
            users.forEach(System.out::println);
        }
    }

    private String promptField(String prompt, String property, UserEntity user) {
        String input;
        boolean valid;
        do {
            System.out.print(prompt + ": ");
            input = scanner.nextLine().trim();

            switch (property) {
                case "name" -> user.setName(input);
                case "email" -> user.setEmail(input);
                case "age" -> {
                    try {
                        user.setAge(Integer.parseInt(input));
                    } catch (NumberFormatException e) {
                        System.out.println("Возраст должен быть числом!");
                        user.setAge(null);
                    }
                }
            }

            valid = validateField(user, property);
        } while (!valid);

        return input;
    }

    private boolean validateField(UserEntity user, String property) {
        Set<ConstraintViolation<UserEntity>> violations = validator.validateProperty(user, property);
        if (!violations.isEmpty()) {
            System.out.println("Ошибки ввода:");
            for (ConstraintViolation<UserEntity> violation : violations) {
                System.out.println(" - " + violation.getMessage());
            }
            return false;
        }
        return true;
    }
}
