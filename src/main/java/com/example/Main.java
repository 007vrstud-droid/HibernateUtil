package com.example;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Главный класс консольного приложения для управления пользователями.
 */
@Slf4j
public class Main {

    private static final UserDao userDao = new UserDao();
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Точка входа в приложение.
     * Запускает консольное меню для выполнения CRUD-операций с пользователями.
     */
    public static void main(String[] args) {
        log.info("user-service start");

        boolean exit = true;

        while (exit) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> createUser();
                case "2" -> readUser();
                case "3" -> updateUser();
                case "4" -> deleteUser();
                case "0" -> {
                    exit = false;
                    log.info("Exit");
                }
                default -> log.warn("Invalid option.");
            }
        }

        scanner.close();
    }

    /**
     * Выводит в консоль меню доступных команд для пользователя.
     */
    private static void printMenu() {
        System.out.println("Choose an option:");
        System.out.println("1 - Create User");
        System.out.println("2 - Read User by ID");
        System.out.println("3 - Update User");
        System.out.println("4 - Delete User");
        System.out.println("0 - Exit");
        System.out.print("Your choice: ");
    }

    /**
     * Создаёт нового пользователя.
     * Пошагово запрашивает у пользователя имя, email и возраст с проверкой корректности ввода:
     */
    private static void createUser() {
        System.out.print("name: ");
        String name = scanner.nextLine();

        String email;
        while (true) {
            System.out.print("email: ");
            email = scanner.nextLine();

            if (!email.contains("@")) {
                log.warn("Email должен содержать символ '@'. Попробуйте ещё раз.");
                continue;
            }

            if (userDao.isEmailExists(email)) {
                log.warn("Такой email уже существует в базе. Попробуйте другой.");
                continue;
            }

            break;
        }

        Integer age = null;
        while (true) {
            System.out.print("age: ");
            String ageInput = scanner.nextLine();

            if (ageInput.isBlank()) {
                break;
            }

            try {
                int parsedAge = Integer.parseInt(ageInput);
                if (parsedAge <= 0 || parsedAge >= 150) {
                    log.warn("Возраст должен быть в диапазоне от 1 до 150. Попробуйте ещё раз.");
                } else {
                    age = parsedAge;
                    break;
                }
            } catch (NumberFormatException e) {
                log.warn("Возраст должен быть числом. Попробуйте ещё раз.");
            }
        }

        User user = new User(null, name, email, age, LocalDateTime.now());
        userDao.saveUser(user);

        log.info("User created: {}", user);
    }

    /**
     * Считывает и выводит данные пользователя по его ID.
     * <p>
     * Если пользователь с указанным ID не найден, выводится предупреждение в лог.
     * </p>
     */
    private static void readUser() {
        System.out.print("Enter user ID: ");
        Long id = Long.parseLong(scanner.nextLine());

        User user = userDao.getUser(id);
        if (user != null) {
            System.out.println(user);
        } else {
            log.warn("User not found with id {}", id);
        }
    }

    /**
     * Обновляет данные существующего пользователя по ID.
     */
    private static void updateUser() {
        System.out.print("Enter user ID to update: ");
        Long id;
        try {
            id = Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            log.warn("Invalid ID input. Must be a number.");
            return;
        }

        User user = userDao.getUser(id);
        if (user == null) {
            log.warn("User not found with id {}", id);
            return;
        }

        System.out.println("Current user data: " + user);
        System.out.println("Enter new values or press Enter to keep existing.");
        System.out.print("Name [" + user.getName() + "]: ");

        String nameInput = scanner.nextLine();
        if (!nameInput.isBlank()) {
            user.setName(nameInput);
        }

        while (true) {
            System.out.print("Email [" + user.getEmail() + "]: ");
            String emailInput = scanner.nextLine();
            if (emailInput.isBlank()) {
                break;
            }
            if (!emailInput.contains("@")) {
                log.warn("Email должен содержать символ '@'. Попробуйте ещё раз.");
                continue;
            }
            if (!emailInput.equals(user.getEmail()) && userDao.isEmailExists(emailInput)) {
                log.warn("Такой email уже существует в базе. Попробуйте другой.");
                continue;
            }
            user.setEmail(emailInput);
            break;
        }

        while (true) {
            System.out.print("Age [" + (user.getAge() != null ? user.getAge() : "not set") + "]: ");
            String ageInput = scanner.nextLine();

            if (ageInput.isBlank()) {
                break;
            }

            try {
                int age = Integer.parseInt(ageInput);
                if (age <= 0 || age >= 150) {
                    log.warn("Возраст должен быть в диапазоне от 1 до 150. Попробуйте ещё раз.");
                } else {
                    user.setAge(age);
                    break;
                }
            } catch (NumberFormatException e) {
                log.warn("Возраст должен быть числом. Попробуйте ещё раз.");
            }
        }

        userDao.updateUser(user);
        log.info("User updated: {}", user);
    }

    /**
     * Удаляет пользователя по указанному ID.
     */
    private static void deleteUser() {
        System.out.print("Enter user ID to delete: ");
        String input = scanner.nextLine();

        try {
            Long id = Long.parseLong(input);
            userDao.deleteUser(id);
            log.info("User with ID {} was deleted (if existed).", id);
        } catch (NumberFormatException e) {
            log.warn("Invalid ID input: '{}'", input);
        } catch (Exception e) {
            log.error("Error deleting user with ID {}", input, e);
        }
    }

}
