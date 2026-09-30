package com.chatapp;

import java.util.Scanner;

/** Console application for registration and login (no GUI). */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Registration ===");
        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine().trim();

        Login login = new Login(firstName, lastName);

        // Username
        String username;
        while (true) {
            System.out.print("Enter a username: ");
            username = scanner.nextLine().trim();
            String msg = login.returnUsernameMessage(username);
            System.out.println(msg);
            if (login.checkUserName(username)) {
                break;
            }
        }

        // Password
        String password;
        while (true) {
            System.out.print("Enter a password: ");
            password = scanner.nextLine();
            String msg = login.returnPasswordMessage(password);
            System.out.println(msg);
            if (login.checkPasswordComplexity(password)) {
                break;
            }
        }
        login.registerUser(username, password);

        // Cell number
        while (true) {
            System.out.print("Enter your cell phone number (e.g. +27838968976): ");
            String cell = scanner.nextLine().trim();
            System.out.println(login.registerCellNumber(cell));
            if (login.checkCellPhoneNumber(cell)) {
                break;
            }
        }

        System.out.println("\nRegistration complete.\n");

        // Login
        System.out.println("=== Login ===");
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Username: ");
            String u = scanner.nextLine().trim();
            System.out.print("Password: ");
            String p = scanner.nextLine();
            System.out.println(login.returnLoginStatus(u, p));
            loggedIn = login.loginUser(u, p);
        }

        scanner.close();
    }
}
