package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        boolean input = false;
        int newPort = 4321;

        while (!input) {
            try {
                System.out.println("Welcome to the chat server!");
                System.out.println("You want to change the port? (y/n)");
                String question = new Scanner(System.in).nextLine();
                if (question.equals("y")) {
                    System.out.println("Enter the new port:");
                    newPort = new Scanner(System.in).nextInt();
                } else {
                    System.out.println("Starting on default port 4321.");
                }
                ServerSocket serverSocket = new ServerSocket(newPort);
                Server server = new Server(serverSocket);
                server.startServer();
                input = true;
            } catch (java.net.BindException e) {
                System.out.println("✕ Port already in use. Please choose another port.");
            } catch (IOException e) {
                System.out.println("✕ Error starting server.");
                e.printStackTrace();
            }
        }
    }

    public Server(ServerSocket serverSocket) {
        try {
            this.serverSocket = serverSocket;
            System.out.println("✓ Server created.");
        } catch (Exception e) {
            System.out.println("✕ Error creating server.");
            e.printStackTrace();
        }
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                String username = clientHandler.getClientUsername();
                System.out.println("✓ "+ username +" connected");

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("✕ Error starting server.");
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}