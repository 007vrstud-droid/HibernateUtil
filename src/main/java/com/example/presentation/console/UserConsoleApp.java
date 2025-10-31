package com.example.presentation.console;

import com.example.repository.UserDaoHibernateImpl;
import com.example.service.UserService;
import com.example.service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class UserConsoleApp {

    private final Scanner scanner = new Scanner(System.in);
    private final UserConsoleInputHandler inputHandler = new UserConsoleInputHandler(scanner);
    private final UserService userService = new UserServiceImpl(new UserDaoHibernateImpl());

    public void run() {
        log.info("User service started.");
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> inputHandler.createUser(userService);
                    case "2" -> inputHandler.readUser(userService);
                    case "3" -> inputHandler.updateUser(userService);
                    case "4" -> inputHandler.deleteUser(userService);
                    case "5" -> inputHandler.getAllUsers(userService);
                    case "0" -> {
                        running = false;
                        log.info("Exit.");
                        System.out.println("Выход из приложения.");
                    }
                    default -> {
                        log.warn("Invalid option: {}", choice);
                        System.out.println("Некорректный выбор. Попробуйте снова.");
                    }
                }
            } catch (Exception e) {
                printError(e);
            }
        }
    }

    private void printError(Throwable error) {
        System.err.println("Ошибка: " + error.getMessage());
        log.error("Ошибка во время выполнения", error);
    }

    private void printMenu() {
        System.out.println("""
                === User Management Menu ===
                1 - Create User
                2 - Read User by ID
                3 - Update User
                4 - Delete User
                5 - Show All Users
                0 - Exit
                Your choice: """);
    }

}
