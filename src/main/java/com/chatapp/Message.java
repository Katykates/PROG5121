package com.chatapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single QuickChat message and keeps the arrays of messages (Parts 2 and 3).
 *
 * References:
 *  - Bray, T. (ed.) (2017) RFC 8259: The JavaScript Object Notation (JSON) Data Interchange
 *    Format. RFC Editor. Available at: https://www.rfc-editor.org/info/rfc8259
 *    (Accessed: 30 September 2026).
 *  - Oracle (n.d.) Class Files (Java SE 11 and JDK 11). Available at:
 *    https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/Files.html
 *    (Accessed: 30 September 2026).
 *  - HowToDoInJava (n.d.) Java 11 Files.writeString(): Writing Text to a File. Available at:
 *    https://howtodoinjava.com/java11/write-string-to-file/ (Accessed: 30 September 2026).
 *  - <add the source(s) you used to read the JSON file back into an array: author/site, year, title, URL, date accessed>
 */
public class Message {

    private static final int MAX_MESSAGE_LENGTH = 250;
    private static final String DEFAULT_STORAGE_FILE = "storedMessages.json";
    private static final String DEFAULT_SENDER = "Unknown sender";
    private static final String NO_STORED_MESSAGES = "No stored messages.";
    private static final String NO_SENT_MESSAGES = "No sent messages.";

    // Matches one JSON line such as:    "recipient": "+27718693002",
    private static final Pattern FIELD_PATTERN = Pattern.compile("^\"([A-Za-z]+)\"\\s*:\\s*(.*?),?$");

    // The four arrays. They start empty and are filled while the program runs (nothing is hard-coded).
    // An ArrayList is Java's resizable array, so it can grow as messages are added.
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final List<String> messageHashes = new ArrayList<>();

    private static final Random random = new Random();
    private static int messagesCreated = 0;
    private static String storageFile = DEFAULT_STORAGE_FILE;
    private static String currentSender = DEFAULT_SENDER;

    // Reuses the cell number check written in Part 1
    private static final Login cellChecker = new Login("", "");

    private final String messageID;
    private final int messageNumber;
    private final String sender;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    /** Creates a message with an auto-generated ID and an auto-incremented message number. */
    public Message(String recipient, String messageText) {
        this(generateMessageID(), messagesCreated++, recipient, messageText);
    }

    /** Creates a message with a given ID and number (used by the unit tests). */
    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this(messageID, messageNumber, currentSender, recipient, messageText);
    }

    private Message(String messageID, int messageNumber, String sender, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.sender = sender;
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
                addSent(this);
                return "Message successfully sent.";
            case 2:
                addDisregarded(this);
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

    /** Details of this message, in the order: ID, hash, recipient, message. */
    public String getDetails() {
        return "Message ID: " + messageID + System.lineSeparator()
                + "Message Hash: " + messageHash + System.lineSeparator()
                + "Recipient: " + recipient + System.lineSeparator()
                + "Message: " + messageText;
    }

    /** Full details of this message, including the sender (used by the stored messages report). */
    public String getFullDetails() {
        return "Message ID: " + messageID + System.lineSeparator()
                + "Message Hash: " + messageHash + System.lineSeparator()
                + "Sender: " + sender + System.lineSeparator()
                + "Recipient: " + recipient + System.lineSeparator()
                + "Message: " + messageText;
    }

    // ---------- The arrays (Part 3) ----------

    private static void addSent(Message message) {
        sentMessages.add(message);
        messageHashes.add(message.messageHash);
    }

    private static void addDisregarded(Message message) {
        disregardedMessages.add(message);
        messageHashes.add(message.messageHash);
    }

    private static void addStored(Message message) {
        storedMessages.add(message);
        messageHashes.add(message.messageHash);
    }

    private static void removeStored(Message message) {
        storedMessages.remove(message);
        messageHashes.remove(message.messageHash);
    }

    /** The text of every message in the Sent Messages array. */
    public static String[] getSentMessageTexts() {
        return textsOf(sentMessages);
    }

    /** The text of every message in the Disregarded Messages array. */
    public static String[] getDisregardedMessageTexts() {
        return textsOf(disregardedMessages);
    }

    /** The text of every message in the Stored Messages array. */
    public static String[] getStoredMessageTexts() {
        return textsOf(storedMessages);
    }

    /** Every message hash in the Message Hash array. */
    public static String[] getMessageHashes() {
        return messageHashes.toArray(new String[0]);
    }

    private static String[] textsOf(List<Message> messages) {
        String[] texts = new String[messages.size()];
        for (int i = 0; i < messages.size(); i++) {
            texts[i] = messages.get(i).messageText;
        }
        return texts;
    }

    // ---------- Stored messages features (Part 3) ----------

    /** Sender and recipient of every stored message. */
    public static String displaySenderAndRecipient() {
        if (storedMessages.isEmpty()) {
            return NO_STORED_MESSAGES;
        }
        StringBuilder output = new StringBuilder();
        for (Message m : storedMessages) {
            output.append("Sender: ").append(m.sender)
                    .append(" | Recipient: ").append(m.recipient)
                    .append(System.lineSeparator());
        }
        return output.toString().trim();
    }

    /** The longest message in the Stored Messages array. */
    public static String getLongestStoredMessage() {
        if (storedMessages.isEmpty()) {
            return NO_STORED_MESSAGES;
        }
        String longest = storedMessages.get(0).messageText;
        for (Message m : storedMessages) {
            if (m.messageText.length() > longest.length()) {
                longest = m.messageText;
            }
        }
        return longest;
    }

    /** Finds a message by its ID in the sent, disregarded and stored arrays. Returns null if not found. */
    public static Message findMessageByID(String messageID) {
        List<Message> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(disregardedMessages);
        all.addAll(storedMessages);
        for (Message m : all) {
            if (m.messageID.equals(messageID)) {
                return m;
            }
        }
        return null;
    }

    /** The recipient and message for a message ID, ready to display. */
    public static String searchByMessageID(String messageID) {
        Message found = findMessageByID(messageID);
        if (found == null) {
            return "No message found with the ID " + messageID + ".";
        }
        return "Recipient: " + found.recipient + System.lineSeparator() + "Message: " + found.messageText;
    }

    /** The text of every sent or stored message for a recipient. */
    public static String[] searchByRecipient(String cell) {
        List<String> found = new ArrayList<>();
        for (Message m : sentMessages) {
            if (m.recipient.equals(cell)) {
                found.add(m.messageText);
            }
        }
        for (Message m : storedMessages) {
            if (m.recipient.equals(cell)) {
                found.add(m.messageText);
            }
        }
        return found.toArray(new String[0]);
    }

    /** Deletes a stored message using its message hash and updates the JSON file. */
    public static String deleteByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            if (m.messageHash.equalsIgnoreCase(hash)) {
                String text = m.messageText;
                removeStored(m);
                writeStoredMessagesToFile();
                return "Message \"" + text + "\" successfully deleted.";
            }
        }
        return "No stored message found with the hash " + hash + ".";
    }

    /** A report listing the full details of every stored message. */
    public static String displayReport() {
        if (storedMessages.isEmpty()) {
            return NO_STORED_MESSAGES;
        }
        String newLine = System.lineSeparator();
        StringBuilder report = new StringBuilder("=== Stored Messages Report ===").append(newLine).append(newLine);
        for (int i = 0; i < storedMessages.size(); i++) {
            report.append("Message ").append(i + 1).append(newLine)
                    .append(storedMessages.get(i).getFullDetails()).append(newLine).append(newLine);
        }
        return report.toString().trim();
    }

    /** A report of all the sent messages: message hash, recipient and message. */
    public static String displaySentReport() {
        if (sentMessages.isEmpty()) {
            return NO_SENT_MESSAGES;
        }
        String newLine = System.lineSeparator();
        StringBuilder report = new StringBuilder("=== Sent Messages Report ===").append(newLine).append(newLine);
        for (int i = 0; i < sentMessages.size(); i++) {
            Message m = sentMessages.get(i);
            report.append("Message ").append(i + 1).append(newLine)
                    .append("Message Hash: ").append(m.messageHash).append(newLine)
                    .append("Recipient: ").append(m.recipient).append(newLine)
                    .append("Message: ").append(m.messageText).append(newLine).append(newLine);
        }
        return report.toString().trim();
    }

    // ---------- JSON storage ----------

    /** Adds this message to the stored messages array and writes them all to the JSON file. */
    public boolean storeMessage() {
        if (!storedMessages.contains(this)) {
            addStored(this);
        }
        return writeStoredMessagesToFile();
    }

    private static boolean writeStoredMessagesToFile() {
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
                + "    \"sender\": \"" + escapeJson(sender) + "\",\n"
                + "    \"recipient\": \"" + escapeJson(recipient) + "\",\n"
                + "    \"message\": \"" + escapeJson(messageText) + "\"\n"
                + "  }";
    }

    /**
     * Reads the JSON file into the Stored Messages array (replacing what is in it now).
     * Returns how many messages were loaded.
     */
    public static int loadStoredMessages() {
        // Empty the array first so loading twice never duplicates messages
        while (!storedMessages.isEmpty()) {
            removeStored(storedMessages.get(0));
        }

        Path file = Path.of(storageFile);
        if (!Files.exists(file)) {
            return 0;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            return 0;
        }

        Map<String, String> fields = new HashMap<>();
        int loaded = 0;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.equals("{")) {
                fields.clear();
            } else if (line.startsWith("}")) {
                Message message = fromFields(fields);
                if (message != null) {
                    addStored(message);
                    loaded++;
                }
                fields.clear();
            } else {
                Matcher matcher = FIELD_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String value = matcher.group(2);
                    if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                        value = unescapeJson(value.substring(1, value.length() - 1));
                    }
                    fields.put(matcher.group(1), value);
                }
            }
        }
        return loaded;
    }

    /** Builds a message from the fields read from one JSON object. Returns null if it is incomplete. */
    private static Message fromFields(Map<String, String> fields) {
        String id = fields.get("messageID");
        String text = fields.get("message");
        if (id == null || text == null) {
            return null;
        }
        int number;
        try {
            number = Integer.parseInt(fields.getOrDefault("messageNumber", "0"));
        } catch (NumberFormatException e) {
            number = 0;
        }
        return new Message(id, number,
                fields.getOrDefault("sender", DEFAULT_SENDER),
                fields.getOrDefault("recipient", ""),
                text);
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

    /** Reverses escapeJson: turns escape sequences in the file back into the real characters. */
    private static String unescapeJson(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                switch (next) {
                    case 'n':
                        result.append('\n');
                        break;
                    case 'r':
                        result.append('\r');
                        break;
                    case 't':
                        result.append('\t');
                        break;
                    case 'b':
                        result.append('\b');
                        break;
                    case 'f':
                        result.append('\f');
                        break;
                    case 'u':
                        if (i + 5 <= text.length()) {
                            try {
                                result.append((char) Integer.parseInt(text.substring(i + 1, i + 5), 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                result.append(next);
                            }
                        } else {
                            result.append(next);
                        }
                        break;
                    default:
                        result.append(next);   // handles \" and \\ and \/
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // ---------- Helpers for the app and the tests ----------

    /** Clears all the arrays and resets the counters (used by the unit tests). */
    public static void resetAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messagesCreated = 0;
        storageFile = DEFAULT_STORAGE_FILE;
        currentSender = DEFAULT_SENDER;
    }

    /** Changes the JSON file that messages are stored in (used by the unit tests). */
    public static void setStorageFile(String path) {
        storageFile = path;
    }

    /** Sets the name of the logged-in user, which is recorded as the sender of new messages. */
    public static void setSender(String senderName) {
        currentSender = senderName;
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

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }
}
