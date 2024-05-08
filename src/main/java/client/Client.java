package client;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import server.ClientHandler;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static server.ClientHandler.clientHandlers;

public class Client {

    private static String serverIP = "localhost";
    private static int serverPort = 4321;
    private static Scanner scan = new Scanner(System.in);
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private static String username;

    private static String password;
    private static String passwordConfirm;

    public static void main(String[] args) throws IOException {
        try {
            LoginRegisterMenu();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("✕ Error creating client.");
        }
    }


    private static void LoginRegisterMenu() throws IOException {
        boolean bucle = false;
        while (!bucle) {
            System.out.println(" " + "Welcome to the chat app." + "\n" + "1. Login" + "\n" + "2. Register" + "\n" + "3. Guest" + "\n" + "4. Server Config" + "\n" + "0. Exit");

            System.out.println("Enter your choice:");
            System.out.print("> ");
            String choice = scan.nextLine();

            switch (choice) {
                case "1":
                    Login();
                    break;
                case "2":
                    Register();
                    break;
                case "3":
                    Guest();
                    break;
                case "4":
                    ServerConfig();
                    break;
                case "0":
                    System.out.println("✓ Exiting chat app.");
                    bucle = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    private static void ServerConfig() {
        // Cambiar la dirección del servidor y el puerto
        System.out.println("Server configuration.");
        boolean bucle = false;
        while (!bucle) {
            System.out.println("1. Change server address" + "\n" + "2. Change server port" + "\n" + "0. Back");

            System.out.println("Enter your choice:");
            System.out.print("> ");
            String choice = scan.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("Enter the server address:");
                    System.out.print("> ");
                    String serverAddress = scan.nextLine();
                    serverIP = serverAddress;
                    break;
                case "2":
                    System.out.println("Enter the server port:");
                    System.out.print("> ");
                    int configServerPort = scan.nextInt();
                    serverPort = configServerPort;
                    break;
                case "0":
                    bucle = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    private void userChatChoice(String receiverUsername, String senderUsername) throws IOException {
        // TODO CHECK ALL ACTIVE USERS TO CHOICE USER TO CHAT
        // TODO: Implement logic to check all active users and allow the user to choose who to chat with

        if (senderUsername.equals(receiverUsername)) {
            System.out.println("You can't chat with yourself.");
            return;
        }

        if (clientHandlers.isEmpty()) {
            System.out.println("There are no active users.");
            return;
        }

        System.out.println("Active users:");
        for (ClientHandler clientHandler : clientHandlers) {
            if (!clientHandler.getClientUsername().equals(senderUsername)) {
                clientHandler.bufferedWriter.write(senderUsername);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            }
        }

        System.out.println("Enter the username of the user you want to chat with:");
        System.out.println("Type /exit to exit.");
        System.out.print(">");

    }

    public static void Register() {
        boolean entrada = false;

        ArrayList<String> symbols = new ArrayList<>(Arrays.asList("!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "_", "=", "+", "{", "}", "[", "]", "|", "\\", ":", ";", "'", "\"", ",", "<", ".", ">", "/", "?"));
        ArrayList<String> numbers = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));

        try {
            Terminal terminal = TerminalBuilder.builder().system(true).dumb(true).build();
            LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();

            while (!entrada) {
                int requirements = 0;
                System.out.println("Register a new account.");
                System.out.print("Username: ");
                username = scan.nextLine();
                password = lineReader.readLine("Password: ", '*');
                passwordConfirm = lineReader.readLine("Confirm password: ", '*');

                if (password.contains(" ")) {
                    System.out.println("Password cannot contain spaces.");
                    requirements--;
                }

                boolean containsSymbol = false;
                for (String symbol : symbols) {
                    if (password.contains(symbol)) {
                        containsSymbol = true;
                        break;
                    }
                }

                if (!containsSymbol) {
                    System.out.println("Password must contain at least one symbol.");
                    requirements--;
                }

                boolean containsNumber = false;
                for (String number : numbers) {
                    if (password.contains(number)) {
                        containsNumber = true;
                        break;
                    }
                }

                if (!containsNumber) {
                    System.out.println("Password must contain at least one number.");
                    requirements--;
                }

                if (password.length() < 8) {
                    System.out.println("Password must be at least 8 characters long.");
                    requirements--;
                }

                if (username.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                    System.out.println("Username and password cannot be empty.");
                } else if (!password.equals(passwordConfirm)) {
                    System.out.println("Passwords do not match.");
                } else {
                    if (requirements < 0) {
                        System.out.println("Password must contain at least one symbol, one number, and be at least 8 characters long.");
                    } else {
                        entrada = true;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Todo: conexión a la base de datos
        if (ClientManager.register(username, password)) {
            System.out.println("✓ Registration successful.");
        } else {
            System.out.println("✕ Registration failed.");
        }
    }


    public static void Login() throws IOException {
        boolean entrada = false;

        while (!entrada) {
            System.out.println("Login to your account.");
            System.out.print("Username: ");
            String username = scan.nextLine();
            System.out.print("Password: ");
            String password = scan.nextLine();

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Username and password cannot be empty.");
            }

            // TODO: Connect to server checking if username or email exists and the password is correct
            if (ClientManager.checkLogin(username, password)) {
                System.out.println("✓ Login successful.");
                System.out.println("Welcome " + username + "!");
                System.out.println("Connecting to server...");
                Socket socket = new Socket(serverIP, serverPort);
                Client clientLogged = new Client(socket, username);
                clientLogged.ListenForMessage();
                clientLogged.sendMessage();
                entrada = true;
            } else {
                System.out.println("✕ Login failed try again.");
            }
        }

    }

    public static void Guest() throws IOException {
        boolean entrada = false;
        while (!entrada) {
            System.out.println("Login to your account.");
            System.out.print("Username: ");
            String username = scan.nextLine();

            if (username.isEmpty()) {
                System.out.println("Username cannot be empty.");
            }

            if (ClientManager.checkUser(username)) {
                System.out.println("Username already exists.");
            } else {
                username = "(Guest) " + username;
                System.out.println("✓ " + username + " logged successful.");

                Socket socket = new Socket(serverIP, serverPort);
                Client clientGuest = new Client(socket, username);
                clientGuest.ListenForMessage();
                clientGuest.sendMessage();
                entrada = true;
            }
        }
    }

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Client.username = username;
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void ConnectionWS() {

    }

    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // TODO: Create client and set login and register with server JPA with PostgreSql


            // TODO: Connect to server checking if username or email exists and the password is correct

            Scanner scan = new Scanner(System.in);
            while (socket.isConnected()) {
                System.out.print("> ");
                String messageToSend = scan.nextLine();
                if (messageToSend.equals("/exit")) {
                    System.out.println("✓ Disconnected from server.");
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void ListenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromClient;
                while (socket.isConnected()) {
                    try {
                        messageFromClient = bufferedReader.readLine();
                        System.out.println(messageFromClient);
                    } catch (Exception e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

