package com.chatapp;

import java.util.Scanner;

/** The QuickChat menu and message-entry flow. Only reached after a successful login. */
public class QuickChat {

    private final Scanner scanner;
    private final String senderName;

    public QuickChat(Scanner scanner, String senderName) {
        this.scanner = scanner;
        this.senderName = senderName;
    }

    public void run() {
        System.out.println();
        System.out.println("Welcome to QuickChat.");

        // Messages sent from now on record the logged-in user as the sender
        Message.setSender(senderName);

        // Read any messages stored in the JSON file into the Stored Messages array
        int loaded = Message.loadStoredMessages();
        if (loaded > 0) {
            System.out.println("Loaded " + loaded + " stored message(s) from file.");
        }

        int limit = readInt("How many messages would you like to enter? ", 1);
        int entered = 0;
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("Please choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            int choice = readInt("Enter your choice: ", 1);

            switch (choice) {
                case 1:
                    entered = sendMessages(limit, entered);
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    running = false;
                    System.out.println("Total number of messages sent: " + Message.returnTotalMessages());
                    System.out.println("Goodbye.");
                    break;
                case 4:
                    storedMessagesMenu();
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, 3 or 4.");
            }
        }
    }

    /** The Stored Messages menu: works with the arrays of messages. */
    private void storedMessagesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== Stored Messages ===");
            System.out.println("1) Display the sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for all messages for a particular recipient");
            System.out.println("5) Delete a message using its message hash");
            System.out.println("6) Display a report of all stored messages");
            System.out.println("7) Display a report of all sent messages");
            System.out.println("8) Back to the main menu");
            int choice = readInt("Enter your choice: ", 1);

            switch (choice) {
                case 1:
                    System.out.println(Message.displaySenderAndRecipient());
                    break;
                case 2:
                    System.out.println(Message.getLongestStoredMessage());
                    break;
                case 3:
                    System.out.print("Enter the message ID: ");
                    String id = scanner.nextLine().trim();
                    System.out.println(Message.searchByMessageID(id));
                    break;
                case 4:
                    System.out.print("Enter the recipient's cell number: ");
                    String cell = scanner.nextLine().trim();
                    String[] found = Message.searchByRecipient(cell);
                    if (found.length == 0) {
                        System.out.println("No messages found for " + cell + ".");
                    } else {
                        for (int i = 0; i < found.length; i++) {
                            System.out.println((i + 1) + ") " + found[i]);
                        }
                    }
                    break;
                case 5:
                    System.out.print("Enter the message hash to delete: ");
                    String hash = scanner.nextLine().trim();
                    System.out.println(Message.deleteByHash(hash));
                    break;
                case 6:
                    System.out.println(Message.displayReport());
                    break;
                case 7:
                    System.out.println(Message.displaySentReport());
                    break;
                case 8:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 8.");
            }
        }
    }

    /** Lets the user enter messages until the number they chose at the start has been reached. */
    private int sendMessages(int limit, int entered) {
        if (entered >= limit) {
            System.out.println("You have already entered all " + limit + " messages.");
            return entered;
        }

        while (entered < limit) {
            System.out.println();
            System.out.println("--- Message " + (entered + 1) + " of " + limit + " ---");

            String recipient = readValidRecipient();
            String text = readValidMessage();

            Message message = new Message(recipient, text);
            System.out.println(message.getMessageIDMessage());

            int option = readSendOption();
            System.out.println(message.SentMessage(option));

            if (option == 1) {
                System.out.println();
                System.out.println(message.getDetails());
            } else if (option == 2) {
                System.out.print("> ");
                String answer = scanner.nextLine().trim();
                System.out.println(answer.equals("0") ? "Message deleted." : "Message not deleted; it was not sent.");
            }
            entered++;
        }

        System.out.println();
        System.out.println("Total number of messages sent: " + Message.returnTotalMessages());
        return entered;
    }

    private String readValidRecipient() {
        while (true) {
            System.out.print("Enter the recipient's cell number (e.g. +27718693002): ");
            String cell = scanner.nextLine().trim();
            if (Message.isRecipientValid(cell)) {
                System.out.println(Message.checkRecipientCell(cell));
                return cell;
            }
            System.out.println(Message.checkRecipientCell(cell));
        }
    }

    private String readValidMessage() {
        while (true) {
            System.out.print("Enter your message (maximum 250 characters): ");
            String text = scanner.nextLine().trim();
            if (text.isEmpty()) {
                System.out.println("The message cannot be empty.");
            } else if (!Message.isMessageLengthValid(text)) {
                System.out.println("Please enter a message of less than 250 characters.");
                System.out.println(Message.checkMessageLength(text));
            } else {
                System.out.println("Message sent");
                return text;
            }
        }
    }

    private int readSendOption() {
        System.out.println("What would you like to do with this message?");
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        while (true) {
            int option = readInt("Enter your choice: ", 1);
            if (option >= 1 && option <= 3) {
                return option;
            }
            System.out.println("Invalid option. Please choose 1, 2 or 3.");
        }
    }

    /** Keeps asking until the user enters a whole number that is at least the minimum. */
    private int readInt(String prompt, int minimum) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= minimum) {
                    return value;
                }
                System.out.println("Please enter a number of " + minimum + " or more.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}
