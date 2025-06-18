# POP3 Client in Java

This project is a simple POP3 client implemented in Java. It allows users to connect to a POP3 mail server, authenticate, and interact with their mailbox (list, retrieve, and delete emails).

## Features

- Connect to a POP3 server using hostname and port
- User authentication (username and password)
- List available emails
- Retrieve and display email content
- Delete emails from the server
- Graceful connection termination

## Requirements

- Java 8 or higher
- Internet connection (for remote mail servers)

## Usage

1. **Compile the code:**
    ```sh
    javac POP3Client.java
    ```

2. **Run the client:**
    ```sh
    java POP3Client <hostname> <port>
    ```

3. **Follow the prompts** to enter your username and password.

## Example

```sh
java POP3Client pop.example.com
```

## Notes

- Ensure your mail server supports plain POP3 connections.
- For SSL/TLS support, additional configuration is required.

## License

This project is licensed under the MIT License.

