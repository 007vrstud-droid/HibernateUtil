package com.example.console;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.exception.InvalidDataException;
import com.example.service.UserService;
import com.example.util.UserChecks;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class UserInputHandler {

    private static final String FIELD_NAME = "name";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_AGE = "age";

    private final Scanner scanner;
    private final Validator validator;

    public UserInputHandler(Scanner scanner) {
        this.scanner = scanner;

        // Инициализация Validator в конструкторе
        ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator()) // убирает зависимость от EL
                .buildValidatorFactory();
        this.validator = factory.getValidator();
    }

    // ================= CREATE =================
    public void createUser(UserService userService) {
        UserCreateRequest request = new UserCreateRequest();

        request.setName(promptField("Имя", FIELD_NAME, request, userService));
        request.setEmail(promptField("Email", FIELD_EMAIL, request, userService));
        request.setAge(Integer.parseInt(promptField("Возраст", FIELD_AGE, request, userService)));

        userService.createUser(request);
        System.out.println("Пользователь успешно создан!");
    }

    // ================= READ =================
    public void readUser(UserService userService) {
        System.out.println("Введите ID пользователя для просмотра: ");
        Long id = readLong();

        Optional<UserResponse> userOpt = userService.getUserById(id);
        if (userOpt.isPresent()) {
            System.out.println("Пользователь найден: " + formatUser(userOpt.get()));
        } else {
            System.out.println("Пользователь с ID " + id + " не найден.");
        }
    }

    // ================= UPDATE =================
    public void updateUser(UserService userService) {
        System.out.println("Введите ID пользователя для обновления: ");
        Long id = readLong();

        Optional<UserResponse> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            System.out.println("Пользователь с ID " + id + " не найден.");
            return;
        }

        UserResponse current = userOpt.get();
        System.out.println("Текущие данные: " + formatUser(current));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(id);

        request.setName(promptField("Имя", FIELD_NAME, request, userService));
        request.setEmail(promptField("Email", FIELD_EMAIL, request, userService));
        request.setAge(Integer.parseInt(promptField("Возраст", FIELD_AGE, request, userService)));

        userService.updateUser(request);
        System.out.println("Пользователь обновлён.");
    }

    // ================= DELETE =================
    public void deleteUser(UserService userService) {
        System.out.println("Введите ID пользователя для удаления: ");
        Long id = readLong();

        Optional<UserResponse> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            System.out.println("Пользователь с ID " + id + " не найден.");
            return;
        }

        userService.deleteUser(id);
        System.out.println("🗑 Пользователь с ID " + id + " удалён.");
    }

    // ================= GET ALL =================
    public void getAllUsers(UserService userService) {
        List<UserResponse> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Пользователи отсутствуют.");
        } else {
            System.out.println("=== Список пользователей ===");
            for (UserResponse u : users) {
                System.out.println(formatUser(u));
            }
        }
    }

    // ================= UTILS =================

    private Long readLong() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод. Введите число: ");
            }
        }
    }

    private String promptField(String prompt, String property, Object dto, UserService userService) {
        String input;
        boolean valid;

        do {
            System.out.print(prompt + ": ");
            input = scanner.nextLine().trim();
            valid = true;

            try {
                switch (property) {
                    case FIELD_NAME -> {
                        if (dto instanceof UserCreateRequest tempCreate) tempCreate.setName(input);
                        if (dto instanceof UserUpdateRequest tempUpdate) tempUpdate.setName(input);
                        valid = validateField(dto, FIELD_NAME);
                    }
                    case FIELD_EMAIL -> {
                        UserChecks.validateEmail(input);

                        boolean exists = userService.isEmailExists(input);
                        if (dto instanceof UserUpdateRequest updateReq) {
                            exists = exists && !input.equals(updateReq.getEmail());
                        }

                        if (exists) {
                            System.out.println("Пользователь с этим email уже существует!");
                            valid = false;
                        } else {
                            if (dto instanceof UserCreateRequest tempCreate) tempCreate.setEmail(input);
                            if (dto instanceof UserUpdateRequest tempUpdate) tempUpdate.setEmail(input);
                            valid = validateField(dto, FIELD_EMAIL);
                        }
                    }
                    case FIELD_AGE -> {
                        Integer age = null;
                        try {
                            age = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            System.out.println("Возраст должен быть числом!");
                            valid = false;
                        }
                        if (valid) {
                            if (dto instanceof UserCreateRequest tempCreate) tempCreate.setAge(age);
                            if (dto instanceof UserUpdateRequest tempUpdate) tempUpdate.setAge(age);
                            valid = validateField(dto, FIELD_AGE);
                        }
                    }
                }
            } catch (InvalidDataException e) {
                System.out.println("Ошибка: " + e.getMessage());
                valid = false;
            }

            if (!valid) {
                System.out.println("Попробуйте снова.");
            }

        } while (!valid);

        return input;
    }

    private boolean validateField(Object dto, String property) {
        Set<ConstraintViolation<Object>> violations = validator.validateProperty(dto, property);
        if (!violations.isEmpty()) {
            System.out.println("Ошибки ввода:");
            for (ConstraintViolation<Object> violation : violations) {
                System.out.println(" - " + violation.getMessage());
            }
            return false;
        }
        return true;
    }

    private String formatUser(UserResponse user) {
        return String.format(
                "[ID: %d, Имя: %s, Email: %s, Возраст: %d, Создан: %s]",
                user.getId(), user.getName(), user.getEmail(), user.getAge(), user.getCreatedAt()
        );
    }
}
