package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private static Socket socket;
    public static BufferedReader bufferedReader;
    public static BufferedWriter bufferedWriter;
    private static String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("▲ - " + clientUsername + " has entered the chat!");
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (Exception e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    // (S) This method is used to broadcast a message to all clients except the client that sent the message
    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (message.equals("/exit")) {
                    removeClientHandler();
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    return;
                }
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    bufferedWriter.write(message);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("✕ Error broadcasting message.");
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    // (WS) This method is used to broadcast a message to all clients except the client that sent the message
    public static void broadcastMessage(String username, String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(username)) {
                    clientHandler.bufferedWriter.write(username + ": " + message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("✕ Error broadcasting message.");
                closeEverything(clientHandler, socket, bufferedReader, bufferedWriter);
            }
        }
    }


    public void sendMessage(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // (S) This method is used to close the socket, BufferedReader, and BufferedWriter
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
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

    // (WS) This method is used to remove the client handler from the list of client handlers and broadcast a message to the other clients

    public static void closeEverything(ClientHandler clientHandler, Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
    clientHandler.removeClientHandler();
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

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("▼ - " + clientUsername + " has left the chat!");
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public void setClientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }
}
