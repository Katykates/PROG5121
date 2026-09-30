package com.chatapp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginTest {

    private Login login;

    @BeforeEach
    void setUp() {
        login = new Login("Kyle", "Smith");
        login.registerUser("kyl_1", "Ch&&sec@ke99!");
    }

    // ---------- assertEquals tests ----------

    @Test
    void usernameCorrectlyFormatted() {
        assertEquals("Username successfully captured.", login.returnUsernameMessage("kyl_1"));
    }

    @Test
    void usernameIncorrectlyFormatted() {
        assertEquals("Username is not correctly formatted; please ensure that your username "
                + "contains an underscore and is no more than five characters in length.",
                login.returnUsernameMessage("kyle!!!!!!!"));
    }

    @Test
    void passwordMeetsComplexity() {
        assertEquals("Password successfully captured.", login.returnPasswordMessage("Ch&&sec@ke99!"));
    }

    @Test
    void passwordDoesNotMeetComplexity() {
        assertEquals("Password is not correctly formatted; please ensure that the password "
                + "contains at least eight characters, a capital letter, a number, and a special character.",
                login.returnPasswordMessage("password"));
    }

    @Test
    void cellNumberCorrectlyFormatted() {
        assertEquals("Cell number successfully captured.", login.registerCellNumber("+27838968976"));
    }

    @Test
    void cellNumberIncorrectlyFormatted() {
        assertEquals("Cell number is incorrectly formatted or does not contain an international code; "
                + "please correct the number and try again.", login.registerCellNumber("08966553"));
    }

    // ---------- assertTrue / assertFalse tests ----------

    @Test
    void loginSuccessful() {
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    void loginFailed() {
        assertFalse(login.loginUser("kyl_1", "wrongPassword1!"));
    }

    @Test
    void usernameCorrectlyFormattedBoolean() {
        assertTrue(login.checkUserName("kyl_1"));
    }

    @Test
    void usernameIncorrectlyFormattedBoolean() {
        assertFalse(login.checkUserName("kyle!!!!!!!"));
    }

    @Test
    void passwordMeetsComplexityBoolean() {
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
    }

    @Test
    void passwordDoesNotMeetComplexityBoolean() {
        assertFalse(login.checkPasswordComplexity("password"));
    }

    @Test
    void cellNumberCorrectlyFormattedBoolean() {
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
    }

    @Test
    void cellNumberIncorrectlyFormattedBoolean() {
        assertFalse(login.checkCellPhoneNumber("08966553"));
    }

    // ---------- Login message tests ----------

    @Test
    void loginStatusSuccess() {
        assertEquals("Welcome Kyle, Smith it is great to see you again.",
                login.returnLoginStatus("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    void loginStatusFailure() {
        assertEquals("Username or password incorrect, please try again.",
                login.returnLoginStatus("kyl_1", "nope"));
    }
}
