package com.example;

import com.example.model.UserEntity;
import com.example.service.UserService;
import com.example.service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

@Slf4j
public class Main {

    private static final UserService userService = new UserServiceImpl();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        log.info("user-service start");

        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> createUser();
                case "2" -> readUser();
                case "3" -> updateUser();
                case "4" -> deleteUser();
                case "5" -> readAllUsers();
                case "0" -> {
                    running = false;
                    log.info("Exit");
                }
                default -> log.warn("Invalid option.");
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== User Management Menu ===");
        System.out.println("1 - Create User");
        System.out.println("2 - Read User by ID");
        System.out.println("3 - Update User");
        System.out.println("4 - Delete User");
        System.out.println("5 - Show All Users");
        System.out.println("0 - Exit");
        System.out.print("Your choice: ");
    }

    private static void createUser() {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        String email;
        while (true) {
            System.out.print("Email: ");
            email = scanner.nextLine().trim();
            if (!email.contains("@")) {
                log.warn("Некорректный email. Попробуйте снова.");
                continue;
            }
            if (userService.isEmailExists(email)) {
                log.warn("Такой email уже существует.");
                continue;
            }
            break;
        }

        Integer age = null;
        while (true) {
            System.out.print("Age: ");
            String input = scanner.nextLine().trim();
            if (input.isBlank()) break;

            try {
                int parsed = Integer.parseInt(input);
                if (parsed <= 0 || parsed >= 150) {
                    log.warn("Возраст должен быть в диапазоне 1–150.");
                } else {
                    age = parsed;
                    break;
                }
            } catch (NumberFormatException e) {
                log.warn("Возраст должен быть числом.");
            }
        }

        UserEntity user = new UserEntity(null, name, email, age, LocalDateTime.now());
        userService.createUser(user);
    }

    private static void readUser() {
        System.out.print("Enter user ID: ");
        try {
            Long id = Long.parseLong(scanner.nextLine());
            UserEntity user = userService.getUserById(id);
            if (user != null) System.out.println(user);
            else log.warn("User not found.");
        } catch (NumberFormatException e) {
            log.warn("Invalid ID format.");
        }
    }

    private static void updateUser() {
        System.out.print("Enter user ID to update: ");
        Long id;
        try {
            id = Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            log.warn("Invalid ID format.");
            return;
        }

        UserEntity existing = userService.getUserById(id);
        if (existing == null) {
            log.warn("User not found.");
            return;
        }

        System.out.println("Current data: " + existing);

        System.out.print("Name [" + existing.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isBlank()) existing.setName(name);

        System.out.print("Email [" + existing.getEmail() + "]: ");
        String email = scanner.nextLine().trim();
        if (!email.isBlank()) existing.setEmail(email);

        System.out.print("Age [" + existing.getAge() + "]: ");
        String ageInput = scanner.nextLine().trim();
        if (!ageInput.isBlank()) {
            try {
                existing.setAge(Integer.parseInt(ageInput));
            } catch (NumberFormatException e) {
                log.warn("Age must be a number.");
            }
        }

        userService.updateUser(existing);
    }

    private static void deleteUser() {
        System.out.print("Enter user ID to delete: ");
        try {
            Long id = Long.parseLong(scanner.nextLine());
            userService.deleteUser(id);
        } catch (NumberFormatException e) {
            log.warn("Invalid ID format.");
        }
    }

    private static void readAllUsers() {
        List<UserEntity> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            System.out.println("=== All Users ===");
            users.forEach(System.out::println);
        }
    }
}