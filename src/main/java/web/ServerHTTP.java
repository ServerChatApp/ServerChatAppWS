package web;

import client.Client;
import client.ClientManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server.ClientHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.Socket;

@SpringBootApplication
public class ServerHTTP extends TextWebSocketHandler {

    public static void main(String[] args) {
        SpringApplication.run(ServerHTTP.class, args);
    }

    @Controller
    @RequestMapping("/")
    public static class ServerController {

        @GetMapping("/")
        public String index() {
            return "/index.html";
        }

        @PostMapping("/login")
        public ResponseEntity<Boolean> handleLogin(@RequestBody LoginRequest request) {
            boolean loginSuccessful = ClientManager.checkLogin(request.getUsername(), request.getPassword());
            return new ResponseEntity<>(loginSuccessful, HttpStatus.OK);
        }

        @PostMapping("/register")
        public ResponseEntity<Boolean> handleRegister(@RequestBody RegisterRequest request) {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
            }
            boolean registerSuccessful = ClientManager.register(request.getUsername(), request.getPassword());
            return new ResponseEntity<>(registerSuccessful, HttpStatus.OK);
        }

        @PostMapping("/chat")
        public String handleChat(@RequestBody MessageRequest request, WebSocketSession session) {
            session.getAttributes().put("username", request.getUsername());
            ClientHandler.broadcastMessage(request.getUsername(), request.getMessage());
            return "Message sent";
        }

        @PostMapping("/logout")
        public String handleLogout(@RequestParam String username) {
            // Use ClientManager to logout user
            ClientManager.logoutUser(username);
            return "Logout successful";
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // This method will be called whenever a chat message is received from a client.
        String username = (String) session.getAttributes().get("username");
        ClientHandler.broadcastMessage(username, message.getPayload());
    }
}