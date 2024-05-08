package client;

import database.Database;
import server.ClientHandler;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Arrays;

import static database.Database.getConnectionString;
import static server.ClientHandler.broadcastMessage;
import static server.ClientHandler.clientHandlers;

public class ClientManager {

    private static Database database;

    static {
        try {
            database = new Database();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private ClientManager() {
    }

    // Método para loguear un nuevo usuario

    public static boolean checkLogin(String username, String password) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = database.getConnection();
            if (connection == null || connection.isClosed()) {
                // Reestablecer la conexión aquí
                String connectionString = getConnectionString();
                connection = DriverManager.getConnection(connectionString);
            }
            statement = connection.prepareStatement("SELECT password FROM users WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                // El usuario no existe
                return false;
            }

            String hashedPassword = resultSet.getString("password");
            return Arrays.equals(hashedPassword.getBytes(StandardCharsets.UTF_8), hashPassword(password).getBytes(StandardCharsets.UTF_8));

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } finally {
            // Cerrar los recursos en un bloque finally para evitar fugas de recursos
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para registrar un nuevo usuario
    public static boolean register(String username, String password) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = database.getConnection();
            if (connection == null || connection.isClosed()) {
                // Reestablecer la conexión aquí
                String connectionString = getConnectionString();
                connection = DriverManager.getConnection(connectionString);
            }
            PreparedStatement checkStatement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
            try {

                // Resto del código para registrar el usuario...
                // Aquí puedes ejecutar las consultas utilizando checkStatement y insertStatement

                // Por ejemplo:
                checkStatement.setString(1, username);
                ResultSet resultSet = checkStatement.executeQuery();
                resultSet.next();
                int count = resultSet.getInt(1);
                if (count > 0) {
                    // El usuario ya existe, devuelve false
                    return false;
                }

                insertStatement.setString(1, username);
                insertStatement.setString(2, hashPassword(password));
                insertStatement.executeUpdate();

                // Registro exitoso, devuelve true
                return true;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // Method to create the users table if it does not exist
    private static void createTable(Connection connection) {
        try {
            PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, username VARCHAR(255) UNIQUE, password VARCHAR(255))");
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }


    // Method to logout a user

    public static void logoutUser(String username) {
        // Código para cerrar la sesión del usuario
        broadcastMessage(username, "/exit");
    }


    // Method to check user is already registered
    public static boolean checkUser(String username) {
        int count = 0;
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = database.getConnection();
            if (connection == null || connection.isClosed()) {
                // Reestablecer la conexión aquí
                String connectionString = getConnectionString();
                connection = DriverManager.getConnection(connectionString);
            }
            statement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            count = resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            // Cerrar los recursos en un bloque finally para evitar fugas de recursos
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

}