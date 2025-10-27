package com.example.console;

import com.example.repository.UserDaoHibernateImpl;
import com.example.service.UserService;
import com.example.service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class UserConsoleApp {

    private final Scanner scanner = new Scanner(System.in);
    private final UserInputHandler inputHandler = new UserInputHandler(scanner);
    private final UserService userService = new UserServiceImpl(new UserDaoHibernateImpl());

    public void run() {
        log.info("Старт программы.");
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
                        log.info("Выход из приложения.");
                    }
                    default -> {
                        log.warn("Некорректный выбор. Попробуйте снова.{}", choice);
                    }
                }
            } catch (Exception e) {
                log.error("Ошибка во время выполнения", e);
            }
        }
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
