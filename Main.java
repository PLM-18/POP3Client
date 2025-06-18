import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    private static POP3Client client;
    private static Scanner scanner = new Scanner(System.in);

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RED = "\u001B[31m";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Main <server>");
            return;
        }

        String server = args[0];
        int port = 110;

        System.out.print(ANSI_CYAN + "+==================================+\n");
        System.out.print("|      POP3 Email Client Login      |\n");
        System.out.print("|___________________________________|\n" + ANSI_RESET);

        System.out.print(ANSI_YELLOW + "Enter username: " + ANSI_RESET);
        String username = scanner.nextLine();
        System.out.print(ANSI_YELLOW + "Enter password: " + ANSI_RESET);
        String password = scanner.nextLine();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            System.err.println(ANSI_RED + "Username and password required." + ANSI_RESET);
            return;
        }

        client = new POP3Client(server, port);
        client.connect();
        login(username, password);

        mainMenu();
    }

    private static void mainMenu() {
        boolean running = true;

        while (running) {
            printMainMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    listEmails();
                    break;
                case "2":
                    viewEmail();
                    break;
                case "3":
                    deleteEmails();
                    break;
                case "4":
                    running = false;
                    disconnect();
                    break;
                default:
                    System.out.println(ANSI_YELLOW + "Invalid option. Please try again." + ANSI_RESET);
            }
        }
    }

    private static void printMainMenu() {
        clearScreen();
        System.out.println(ANSI_CYAN + "+==================================+");
        System.out.println("|      POP3 Email Client Menu       |");
        System.out.println("|=======================================|");
        System.out.println("| " + ANSI_GREEN + "1. List all emails               " + ANSI_CYAN + "|");
        System.out.println("| " + ANSI_GREEN + "2. View email details            " + ANSI_CYAN + "|");
        System.out.println("| " + ANSI_GREEN + "3. Delete emails                 " + ANSI_CYAN + "|");
        System.out.println("| " + ANSI_GREEN + "4. Quit                          " + ANSI_CYAN + "|");
        System.out.println("|___________________________________|" + ANSI_RESET);
        System.out.print(ANSI_YELLOW + "Enter your choice: " + ANSI_RESET);
    }

    private static void listEmails() {
        clearScreen();
        System.out.println(ANSI_CYAN + "+==================================+");
        System.out.println("|           Email List              |");
        System.out.println("|___________________________________|" + ANSI_RESET);

        List<String> messages = getEmailList();
        if (messages.isEmpty()) {
            System.out.println(ANSI_YELLOW + "No emails found." + ANSI_RESET);
        } else {
            for (int i = 0; i < messages.size(); i++) {
                String msg = messages.get(i);
                String[] parts = msg.split(" ");
                if (parts.length >= 2) {
                    try {
                        int messageNumber = Integer.parseInt(parts[0]);
                        int size = Integer.parseInt(parts[1]);
                        System.out.printf(ANSI_GREEN + "[%d]" + ANSI_RESET + " Message #%d - Size: %d bytes\n",
                                i + 1, messageNumber, size);
                    } catch (NumberFormatException e) {
                        System.out.println(ANSI_YELLOW + msg + ANSI_RESET);
                    }
                } else {
                    System.out.println(ANSI_YELLOW + msg + ANSI_RESET);
                }
            }
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void viewEmail() {
        clearScreen();
        System.out.println(ANSI_CYAN + "+==================================+");
        System.out.println("|         View Email Details        |");
        System.out.println("|___________________________________|" + ANSI_RESET);

        List<String> messages = getEmailList();
        if (messages.isEmpty()) {
            System.out.println(ANSI_YELLOW + "No emails to view." + ANSI_RESET);
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        System.out.println(ANSI_YELLOW + "Available emails:" + ANSI_RESET);
        for (int i = 0; i < messages.size(); i++) {
            String msg = messages.get(i);
            String[] parts = msg.split(" ");
            if (parts.length >= 2) {
                try {
                    int messageNumber = Integer.parseInt(parts[0]);
                    int size = Integer.parseInt(parts[1]);
                    System.out.printf(ANSI_GREEN + "[%d]" + ANSI_RESET + " Message #%d - Size: %d bytes\n",
                            i + 1, messageNumber, size);
                } catch (NumberFormatException e) {
                    System.out.println(ANSI_YELLOW + msg + ANSI_RESET);
                }
            } else {
                System.out.println(ANSI_YELLOW + msg + ANSI_RESET);
            }
        }

        System.out.print(ANSI_YELLOW + "\nEnter message number to view (or 0 to cancel): " + ANSI_RESET);
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice > 0 && choice <= messages.size()) {
                String msgInfo = messages.get(choice - 1);
                String[] parts = msgInfo.split(" ");
                int actualMessageNumber = Integer.parseInt(parts[0]);

                clearScreen();
                System.out.println(ANSI_CYAN + "+==================================+");
                System.out.println("|         Email Content             |");
                System.out.println("|___________________________________|" + ANSI_RESET);

                String content = getEmail(actualMessageNumber);

                String[] lines = content.split("\\n");
                boolean inHeaders = true;

                for (String line : lines) {
                    if (line.trim().isEmpty() && inHeaders) {
                        inHeaders = false;
                        System.out.println(
                                ANSI_PURPLE + "-------------------- CONTENT --------------------" + ANSI_RESET);
                        continue;
                    }

                    if (inHeaders) {
                        if (line.contains(":")) {
                            String[] headerParts = line.split(":", 2);
                            System.out.println(ANSI_BLUE + headerParts[0] + ":" + ANSI_RESET + headerParts[1]);
                        } else {
                            System.out.println(line);
                        }
                    } else {
                        System.out.println(line);
                    }
                }
            } else if (choice != 0) {
                System.out.println(ANSI_YELLOW + "Invalid message number." + ANSI_RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println(ANSI_YELLOW + "Please enter a valid number." + ANSI_RESET);
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void deleteEmails() {
        clearScreen();
        System.out.println(ANSI_CYAN + "+==================================+");
        System.out.println("|         Delete Emails             |");
        System.out.println("|___________________________________|" + ANSI_RESET);

        List<String> messages = getEmailList();
        if (messages.isEmpty()) {
            System.out.println(ANSI_YELLOW + "No emails to delete." + ANSI_RESET);
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        System.out.println(ANSI_YELLOW + "Available emails:" + ANSI_RESET);
        for (int i = 0; i < messages.size(); i++) {
            String msg = messages.get(i);
            String[] parts = msg.split(" ");
            if (parts.length >= 2) {
                try {
                    int messageNumber = Integer.parseInt(parts[0]);
                    int size = Integer.parseInt(parts[1]);
                    System.out.printf(ANSI_GREEN + "[%d]" + ANSI_RESET + " Message #%d - Size: %d bytes\n",
                            i + 1, messageNumber, size);
                } catch (NumberFormatException e) {
                    System.out.println(ANSI_YELLOW + msg + ANSI_RESET);
                }
            } else {
                System.out.println(ANSI_YELLOW + msg + ANSI_RESET);
            }
        }

        System.out.println(ANSI_YELLOW
                + "\nEnter message numbers to delete (comma-separated, or 'all' for all messages, or 0 to cancel): "
                + ANSI_RESET);
        String input = scanner.nextLine().trim();

        if (input.equals("0")) {
            return;
        }

        if (input.equalsIgnoreCase("all")) {
            System.out.print(ANSI_YELLOW + "Are you sure you want to delete ALL emails? (y/n): " + ANSI_RESET);
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (confirm.equals("y") || confirm.equals("yes")) {
                for (String msg : messages) {
                    String[] parts = msg.split(" ");
                    if (parts.length >= 2) {
                        try {
                            int messageNumber = Integer.parseInt(parts[0]);
                            deleteEmail(messageNumber);
                            System.out.println(ANSI_GREEN + "Deleted message #" + messageNumber + ANSI_RESET);
                        } catch (NumberFormatException e) {
                            System.out.println(ANSI_YELLOW + "Error deleting message: " + msg + ANSI_RESET);
                        }
                    }
                }
            }
        } else {
            String[] selectedIndexes = input.split(",");
            List<Integer> messageNumbersToDelete = new ArrayList<>();

            for (String indexStr : selectedIndexes) {
                try {
                    int index = Integer.parseInt(indexStr.trim());
                    if (index > 0 && index <= messages.size()) {
                        String msgInfo = messages.get(index - 1);
                        String[] parts = msgInfo.split(" ");
                        int actualMessageNumber = Integer.parseInt(parts[0]);
                        messageNumbersToDelete.add(actualMessageNumber);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ANSI_YELLOW + "Invalid input: " + indexStr + ANSI_RESET);
                }
            }

            if (!messageNumbersToDelete.isEmpty()) {
                System.out.print(ANSI_YELLOW + "Are you sure you want to delete " + messageNumbersToDelete.size() +
                        " email(s)? (y/n): " + ANSI_RESET);
                String confirm = scanner.nextLine().trim().toLowerCase();
                if (confirm.equals("y") || confirm.equals("yes")) {
                    for (int messageNumber : messageNumbersToDelete) {
                        deleteEmail(messageNumber);
                        System.out.println(ANSI_GREEN + "Deleted message #" + messageNumber + ANSI_RESET);
                    }
                }
            }
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void login(String username, String password) {
        client.login(username, password);
    }

    public static List<String> getEmailList() {
        return client.listMessages();
    }

    public static String getEmail(int messageNumber) {
        return client.retrieveMessage(messageNumber);
    }

    public static void deleteEmail(int messageNumber) {
        client.deleteMessage(messageNumber);
    }

    public static void disconnect() {
        client.disconnect();
        System.out.println(ANSI_GREEN + "Thank you for using POP3 Email Client. Goodbye!" + ANSI_RESET);
    }
}