package com.chatapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a single QuickChat message (Part 2).
 *
 * References (replace with the sources you actually used):
 *  - <add the source(s) you used to research storing messages in JSON: author/site, year, title, URL, date accessed>
 */
public class Message {

    private static final int MAX_MESSAGE_LENGTH = 250;
    private static final String DEFAULT_STORAGE_FILE = "storedMessages.json";

    // Shared for as long as the program is running
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final Random random = new Random();
    private static int messagesCreated = 0;
    private static String storageFile = DEFAULT_STORAGE_FILE;

    // Reuses the cell number check written in Part 1
    private static final Login cellChecker = new Login("", "");

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    /** Creates a message with an auto-generated ID and an auto-incremented message number. */
    public Message(String recipient, String messageText) {
        this(generateMessageID(), messagesCreated++, recipient, messageText);
    }

    /** Creates a message with a given ID and number (used by the unit tests). */
    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
    }

    // ---------- Checks ----------

    /** The message ID must not be more than ten characters long. */
    public boolean checkMessageID() {
        return messageID != null && !messageID.isEmpty() && messageID.length() <= 10;
    }

    /** Returns the message shown when a message ID has been generated. */
    public String getMessageIDMessage() {
        return "Message ID generated: " + messageID;
    }

    /** True if the recipient has an international code and no more than ten digits after it. */
    public static boolean isRecipientValid(String cell) {
        return cellChecker.checkCellPhoneNumber(cell);
    }

    /** Returns the recipient cell number message for the given number. */
    public static String checkRecipientCell(String cell) {
        if (isRecipientValid(cell)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.";
    }

    /** Checks this message's recipient. */
    public String checkRecipientCell() {
        return checkRecipientCell(recipient);
    }

    /** True if the text is not longer than 250 characters. */
    public static boolean isMessageLengthValid(String text) {
        return text != null && text.length() <= MAX_MESSAGE_LENGTH;
    }

    /** Returns the message-length result for the given text. */
    public static String checkMessageLength(String text) {
        if (isMessageLengthValid(text)) {
            return "Message ready to send.";
        }
        int excess = text.length() - MAX_MESSAGE_LENGTH;
        return "Message exceeds 250 characters by " + excess + "; please reduce the size.";
    }

    // ---------- Hash and ID ----------

    /** First two ID digits : message number : first word + last word, all in capitals. */
    public String createMessageHash() {
        String[] words = messageText.trim().split("\\s+");
        String first = cleanWord(words[0]);
        String last = cleanWord(words[words.length - 1]);
        String start = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
        return (start + ":" + messageNumber + ":" + first + last).toUpperCase();
    }

    /** Removes punctuation from the start and end of a word, e.g. "tonight?" becomes "tonight". */
    private static String cleanWord(String word) {
        return word.replaceAll("^[^A-Za-z0-9]+|[^A-Za-z0-9]+$", "");
    }

    /** Generates a random ten-digit message ID. */
    private static String generateMessageID() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            id.append(random.nextInt(10));
        }
        return id.toString();
    }

    // ---------- Send / discard / store ----------

    /**
     * Applies the user's choice: 1 = send, 2 = disregard, 3 = store to send later.
     * Returns the message to show the user.
     */
    public String SentMessage(int option) {
        switch (option) {
            case 1:
                sentMessages.add(this);
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                if (storeMessage()) {
                    return "Message successfully stored.";
                }
                return "Message could not be stored.";
            default:
                return "Invalid option. Please choose 1, 2 or 3.";
        }
    }

    /** Returns the details of all the messages sent while the program is running. */
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
        StringBuilder output = new StringBuilder();
        for (Message m : sentMessages) {
            output.append(m.getDetails()).append(System.lineSeparator()).append(System.lineSeparator());
        }
        return output.toString().trim();
    }

    /** Returns the total number of messages sent. */
    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    /** Full details of this message, in the order: ID, hash, recipient, message. */
    public String getDetails() {
        return "Message ID: " + messageID + System.lineSeparator()
                + "Message Hash: " + messageHash + System.lineSeparator()
                + "Recipient: " + recipient + System.lineSeparator()
                + "Message: " + messageText;
    }

    // ---------- JSON storage ----------

    /** Adds this message to the stored messages and writes them all to a JSON file. */
    public boolean storeMessage() {
        if (!storedMessages.contains(this)) {
            storedMessages.add(this);
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < storedMessages.size(); i++) {
            json.append(i == 0 ? "\n" : ",\n").append(storedMessages.get(i).toJson());
        }
        json.append("\n]\n");
        try {
            Files.writeString(Path.of(storageFile), json.toString());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String toJson() {
        return "  {\n"
                + "    \"messageID\": \"" + escapeJson(messageID) + "\",\n"
                + "    \"messageNumber\": " + messageNumber + ",\n"
                + "    \"messageHash\": \"" + escapeJson(messageHash) + "\",\n"
                + "    \"recipient\": \"" + escapeJson(recipient) + "\",\n"
                + "    \"message\": \"" + escapeJson(messageText) + "\"\n"
                + "  }";
    }

    /** Escapes characters that are not allowed to appear as they are inside a JSON string. */
    private static String escapeJson(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        result.append(String.format("\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }
        return result.toString();
    }

    // ---------- Helpers for the app and the tests ----------

    /** Clears all sent and stored messages and resets the counters (used by the unit tests). */
    public static void resetAll() {
        sentMessages.clear();
        storedMessages.clear();
        messagesCreated = 0;
        storageFile = DEFAULT_STORAGE_FILE;
    }

    /** Changes the JSON file that messages are stored in (used by the unit tests). */
    public static void setStorageFile(String path) {
        storageFile = path;
    }

    public String getMessageID() {
        return messageID;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }
}
