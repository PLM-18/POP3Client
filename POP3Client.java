import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class POP3Client {
    private String server;
    private int port;
    private Socket socket;
    private BufferedReader reader;

    public POP3Client(String server, int port) {
        this.server = server;
        this.port = port;
    }

    public void connect() {
        try {
            socket = new Socket(server, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to " + server + " on port " + port);
            System.out.println("Server: " + reader.readLine());
        } catch (Exception e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    public void login(String username, String password) {
        try {
            sendCommand("USER " + username);
            sendCommand("PASS " + password);
        } catch (Exception e) {
            System.err.println("Error logging in: " + e.getMessage());
        }
    }

    public List<String> listMessages() {
        List<String> messages = new ArrayList<>();
        try {
            sendCommand("LIST");
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(".")) break;
                messages.add(line);
            }
        } catch (Exception e) {
            System.err.println("Error listing messages: " + e.getMessage());
        }
        return messages;
    }

    public String retrieveMessage(int messageNumber) {
        StringBuilder message = new StringBuilder();
        try {
            sendCommand("RETR " + messageNumber);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(".")) break;
                message.append(line).append("\n");
            }
        } catch (Exception e) {
            System.err.println("Error retrieving message: " + e.getMessage());
        }
        return message.toString();
    }

    public void deleteMessage(int messageNumber) {
        try {
            sendCommand("DELE " + messageNumber);
        } catch (Exception e) {
            System.err.println("Error deleting message: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            sendCommand("QUIT");
            socket.close();
            System.out.println("Disconnected from server.");
        } catch (Exception e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    private void sendCommand(String command) throws Exception {
        socket.getOutputStream().write((command + "\r\n").getBytes());
        socket.getOutputStream().flush();
        String response = reader.readLine();
        System.out.println("Server: " + response);
        if (!response.startsWith("+")) {
            throw new Exception("Error: " + response);
        }
    }
}
