# QuickChat (PROG5121 PoE)

A console-based chat application written in Java, built in three parts:

1. **Registration and login** - username, password and cell number validation, then login.
2. **Sending messages** - a QuickChat menu where a logged-in user can send, disregard or store messages.
3. **Stored data and reports** - arrays of sent, disregarded and stored messages and message hashes, with
   search, delete and report features. Stored messages are saved to and read from a JSON file.

## Requirements
- Java 17 or later
- Maven

## Run the unit tests
```
mvn test
```

## Run the application
```
mvn compile
java -cp target/classes com.chatapp.Main
```

## Version control and automation
- Each part was built in commits of its own, with feature branches used to keep `main` working.
- GitHub Actions runs the unit tests on every push.
