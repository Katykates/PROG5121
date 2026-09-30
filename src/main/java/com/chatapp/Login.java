package com.chatapp;

import java.util.regex.Pattern;

/**
 * Handles user registration and login for the Chat App (Part 1).
 *
 * References (replace/add your own sources here):
 *  - Regex for cell number: <add the website/tutorial you used, author, year, URL, date accessed>
 *  - Password checking approach: <add source if you used one>
 */
public class Login {

    // Regex: "+" then a country code (1-3 digits) then a number of no more than 10 digits.
    // e.g. +27838968976  -> valid,  08966553 -> invalid (no international code)
    private static final Pattern CELL_PATTERN = Pattern.compile("^\\+\\d{1,3}\\d{1,10}$");

    private String username;
    private String password;
    private String cellNumber;
    private final String firstName;
    private final String lastName;

    public Login(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // ---------- Validation methods ----------

    /** Username must contain an underscore and be no more than five characters long. */
    public boolean checkUserName(String username) {
        return username != null && username.contains("_") && username.length() <= 5;
    }

    /** Password: at least 8 chars, a capital letter, a number and a special character. */
    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasCapital = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasCapital = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
            }
        }
        return hasCapital && hasNumber && hasSpecial;
    }

    /** Cell number must start with the international code (+xx) followed by no more than ten digits. */
    public boolean checkCellPhoneNumber(String cellNumber) {
        return cellNumber != null && CELL_PATTERN.matcher(cellNumber).matches();
    }

    // ---------- Registration ----------

    /** Returns the registration message for the username and password, and stores them if both are valid. */
    public String registerUser(String username, String password) {
        if (!checkUserName(username)) {
            return "Username is not correctly formatted; please ensure that your username "
                    + "contains an underscore and is no more than five characters in length.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted; please ensure that the password "
                    + "contains at least eight characters, a capital letter, a number, and a special character.";
        }
        this.username = username;
        this.password = password;
        return "Username successfully captured.\nPassword successfully captured.";
    }

    /** Returns the message for the username check only. */
    public String returnUsernameMessage(String username) {
        return checkUserName(username)
                ? "Username successfully captured."
                : "Username is not correctly formatted; please ensure that your username "
                  + "contains an underscore and is no more than five characters in length.";
    }

    /** Returns the message for the password check only. */
    public String returnPasswordMessage(String password) {
        return checkPasswordComplexity(password)
                ? "Password successfully captured."
                : "Password is not correctly formatted; please ensure that the password "
                  + "contains at least eight characters, a capital letter, a number, and a special character.";
    }

    /** Validates and stores the cell number, returning the appropriate message. */
    public String registerCellNumber(String cellNumber) {
        if (checkCellPhoneNumber(cellNumber)) {
            this.cellNumber = cellNumber;
            return "Cell number successfully captured.";
        }
        return "Cell number is incorrectly formatted or does not contain an international code; "
                + "please correct the number and try again.";
    }

    // ---------- Login ----------

    /** True only if the details match those stored at registration. */
    public boolean loginUser(String username, String password) {
        return this.username != null
                && this.password != null
                && this.username.equals(username)
                && this.password.equals(password);
    }

    /** Returns the login status message. */
    public String returnLoginStatus(String username, String password) {
        if (loginUser(username, password)) {
            return "Welcome " + firstName + ", " + lastName + " it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }

    public String getCellNumber() {
        return cellNumber;
    }
}
