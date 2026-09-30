package com.chatapp;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StoredMessagesTest {

    private static final String TEXT_1 = "Did you get the cake?";
    private static final String TEXT_2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String TEXT_3 = "Yohoooo, I am at your gate.";
    private static final String TEXT_4 = "It is dinner time!";
    private static final String TEXT_5 = "Ok, I am leaving without you.";

    private static final String RECIPIENT_1 = "+27834557896";
    private static final String RECIPIENT_2 = "+27838884567";
    private static final String RECIPIENT_3 = "+27834484567";
    private static final String RECIPIENT_5 = "+27838884567";

    // The brief gives message 4's developer entry (0838884567) but no recipient, so a different one is used.
    private static final String ID_4 = "0838884567";
    private static final String RECIPIENT_4 = "+27831234567";

    @TempDir
    Path tempDir;

    private Path jsonFile;
    private Message message1;
    private Message message2;
    private Message message3;
    private Message message4;
    private Message message5;

    @BeforeEach
    void setUp() {
        Message.resetAll();
        jsonFile = tempDir.resolve("storedMessages.json");
        Message.setStorageFile(jsonFile.toString());
        Message.setSender("Kyle Smith");
    }

    /** Developer entry for test data messages 1 to 4. */
    private void loadTestData1To4() {
        message1 = new Message(RECIPIENT_1, TEXT_1);
        message1.SentMessage(1);      // Flag: Sent

        message2 = new Message(RECIPIENT_2, TEXT_2);
        message2.SentMessage(3);      // Flag: Stored

        message3 = new Message(RECIPIENT_3, TEXT_3);
        message3.SentMessage(2);      // Flag: Disregard

        message4 = new Message(ID_4, 3, RECIPIENT_4, TEXT_4);
        message4.SentMessage(1);      // Flag: Sent
    }

    /** Developer entry for test data message 5. */
    private void loadTestData5() {
        message5 = new Message(RECIPIENT_5, TEXT_5);
        message5.SentMessage(3);      // Flag: Stored
    }

    // ---------- The arrays ----------

    @Test
    void sentMessagesArrayCorrectlyPopulated() {
        loadTestData1To4();
        assertArrayEquals(new String[] {TEXT_1, TEXT_4}, Message.getSentMessageTexts());
    }

    @Test
    void disregardedMessagesArrayCorrectlyPopulated() {
        loadTestData1To4();
        assertArrayEquals(new String[] {TEXT_3}, Message.getDisregardedMessageTexts());
    }

    @Test
    void storedMessagesArrayCorrectlyPopulated() {
        loadTestData1To4();
        loadTestData5();
        assertArrayEquals(new String[] {TEXT_2, TEXT_5}, Message.getStoredMessageTexts());
    }

    @Test
    void messageHashArrayContainsAllHashes() {
        loadTestData1To4();
        loadTestData5();
        assertArrayEquals(new String[] {
            message1.getMessageHash(), message2.getMessageHash(), message3.getMessageHash(),
            message4.getMessageHash(), message5.getMessageHash()
        }, Message.getMessageHashes());
    }

    // ---------- Stored messages features ----------

    @Test
    void displayLongestMessage() {
        loadTestData1To4();
        assertEquals(TEXT_2, Message.getLongestStoredMessage());
    }

    @Test
    void searchForMessageID() {
        loadTestData1To4();
        Message found = Message.findMessageByID(ID_4);
        assertNotNull(found);
        assertEquals(TEXT_4, found.getMessageText());
        assertTrue(Message.searchByMessageID(ID_4).contains(TEXT_4));
        assertTrue(Message.searchByMessageID(ID_4).contains(RECIPIENT_4));
    }

    @Test
    void searchForMessageIDNotFound() {
        loadTestData1To4();
        assertNull(Message.findMessageByID("0000000000"));
        assertEquals("No message found with the ID 0000000000.", Message.searchByMessageID("0000000000"));
    }

    @Test
    void searchAllMessagesForARecipient() {
        loadTestData1To4();
        loadTestData5();
        assertArrayEquals(new String[] {TEXT_2, TEXT_5}, Message.searchByRecipient("+27838884567"));
    }

    @Test
    void searchForRecipientWithNoMessages() {
        loadTestData1To4();
        assertEquals(0, Message.searchByRecipient("+27000000000").length);
    }

    @Test
    void displaySenderAndRecipientOfStoredMessages() {
        loadTestData1To4();
        loadTestData5();
        String output = Message.displaySenderAndRecipient();
        assertTrue(output.contains("Sender: Kyle Smith | Recipient: " + RECIPIENT_2));
        assertTrue(output.contains("Sender: Kyle Smith | Recipient: " + RECIPIENT_5));
        assertFalse(output.contains(RECIPIENT_1));
    }

    @Test
    void deleteMessageUsingHash() throws IOException {
        loadTestData1To4();
        loadTestData5();

        String result = Message.deleteByHash(message2.getMessageHash());

        assertEquals("Message \"" + TEXT_2 + "\" successfully deleted.", result);
        assertArrayEquals(new String[] {TEXT_5}, Message.getStoredMessageTexts());
        assertFalse(java.util.Arrays.asList(Message.getMessageHashes()).contains(message2.getMessageHash()));
        assertFalse(Files.readString(jsonFile).contains(TEXT_2));
    }

    @Test
    void deleteMessageUsingUnknownHash() {
        loadTestData1To4();
        assertEquals("No stored message found with the hash 99:9:NOPE.", Message.deleteByHash("99:9:NOPE"));
        assertArrayEquals(new String[] {TEXT_2}, Message.getStoredMessageTexts());
    }

    @Test
    void reportListsFullDetailsOfStoredMessages() {
        loadTestData1To4();
        loadTestData5();
        String report = Message.displayReport();

        for (Message stored : new Message[] {message2, message5}) {
            assertTrue(report.contains("Message ID: " + stored.getMessageID()));
            assertTrue(report.contains("Message Hash: " + stored.getMessageHash()));
            assertTrue(report.contains("Sender: Kyle Smith"));
            assertTrue(report.contains("Recipient: " + stored.getRecipient()));
            assertTrue(report.contains("Message: " + stored.getMessageText()));
        }
        assertFalse(report.contains(TEXT_1));
    }

    @Test
    void reportWithNoStoredMessages() {
        assertEquals("No stored messages.", Message.displayReport());
    }

    @Test
    void sentReportShowsHashRecipientAndMessageOfSentMessages() {
        loadTestData1To4();
        String report = Message.displaySentReport();

        for (Message sent : new Message[] {message1, message4}) {
            assertTrue(report.contains("Message Hash: " + sent.getMessageHash()));
            assertTrue(report.contains("Recipient: " + sent.getRecipient()));
            assertTrue(report.contains("Message: " + sent.getMessageText()));
        }
        // Stored and disregarded messages are not part of the sent report
        assertFalse(report.contains(TEXT_2));
        assertFalse(report.contains(TEXT_3));
    }

    @Test
    void sentReportWithNoSentMessages() {
        assertEquals("No sent messages.", Message.displaySentReport());
    }

    // ---------- Reading the JSON file into the array ----------

    @Test
    void storedMessagesReadBackFromJsonFile() {
        loadTestData1To4();
        loadTestData5();
        String[] hashesBefore = {message2.getMessageHash(), message5.getMessageHash()};

        // Start a fresh "run" of the program that only has the JSON file
        Message.resetAll();
        Message.setStorageFile(jsonFile.toString());
        assertEquals(0, Message.getStoredMessageTexts().length);

        int loaded = Message.loadStoredMessages();

        assertEquals(2, loaded);
        assertArrayEquals(new String[] {TEXT_2, TEXT_5}, Message.getStoredMessageTexts());
        assertArrayEquals(hashesBefore, Message.getMessageHashes());
        assertEquals("Kyle Smith", Message.findMessageByID(message2.getMessageID()).getSender());
        assertEquals(RECIPIENT_2, Message.findMessageByID(message2.getMessageID()).getRecipient());
    }

    @Test
    void loadingTwiceDoesNotDuplicateMessages() {
        loadTestData1To4();
        Message.loadStoredMessages();
        Message.loadStoredMessages();
        assertArrayEquals(new String[] {TEXT_2}, Message.getStoredMessageTexts());
        assertEquals(1, Message.getMessageHashes().length - Message.getSentMessageTexts().length
                - Message.getDisregardedMessageTexts().length);
    }

    @Test
    void specialCharactersSurviveTheJsonFile() {
        String tricky = "She said \"hi\" \\ then left, ok?";
        Message message = new Message("+27834557896", tricky);
        message.SentMessage(3);

        Message.resetAll();
        Message.setStorageFile(jsonFile.toString());
        Message.loadStoredMessages();

        assertArrayEquals(new String[] {tricky}, Message.getStoredMessageTexts());
    }

    @Test
    void loadingWhenThereIsNoFileGivesAnEmptyArray() {
        assertEquals(0, Message.loadStoredMessages());
        assertEquals(0, Message.getStoredMessageTexts().length);
    }
}
