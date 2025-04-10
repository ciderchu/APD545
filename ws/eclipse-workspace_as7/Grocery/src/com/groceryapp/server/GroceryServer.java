/**********************************************
Workshop #7
Course: APD545
Last Name: Chu
First Name: Sin Kau
ID: 155131220
Section: NDD
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature Sin Kau Chu
Date: 31-Mar-2025
**********************************************/

package com.groceryapp.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.util.concurrent.*;

public class GroceryServer {
    private static final int PORT = 8888;
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/grocery_app?useSSL=false";
    private static final String DATABASE_USERNAME = "root";
    private static final String DATABASE_PASSWORD = "Rg01151021!";
    
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private ConcurrentHashMap<String, ClientHandler> connectedClients;
    
    public GroceryServer() {
        this.threadPool = Executors.newCachedThreadPool();
        this.connectedClients = new ConcurrentHashMap<>();
    }
    
    public void start() {
        try {
            // creating server socket
            serverSocket = new ServerSocket(PORT);
            System.out.println("Grocery Server started on port " + PORT);
            System.out.println("Waiting for clients to connect...");
            
            // continuously accepting client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                // creating and starting a new thread for each client
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stop();
        }
    }
    
    public void stop() {
        try {
            // closing resources
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // shutting down thread pool
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdown();
                try {
                    if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                        threadPool.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    threadPool.shutdownNow();
                }
            }
            
            System.out.println("Server stopped");
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }
    
    // register a client with the server
    public void registerClient(String username, ClientHandler handler) {
        connectedClients.put(username, handler);
        broadcastMessage("SERVER", username + " has joined the server.");
        displayConnectedClients();
    }
    
    // remove a client from the server
    public void removeClient(String username) {
        connectedClients.remove(username);
        broadcastMessage("SERVER", username + " has left the server.");
        displayConnectedClients();
    }
    
    // display all connected clients
    public void displayConnectedClients() {
        System.out.println("\nConnected Clients: " + connectedClients.size());
        for (String username : connectedClients.keySet()) {
            System.out.println("- " + username);
        }
        System.out.println();
    }
    
    // broadcast a message to all connected clients
    public void broadcastMessage(String sender, String message) {
        String formattedMessage = sender + ": " + message;
        System.out.println(formattedMessage);
        
        for (ClientHandler handler : connectedClients.values()) {
            handler.sendMessage(formattedMessage);
        }
    }
    
    // authenticate a user against the database
    public int authenticateUser(String username, String password) {
        try (Connection connection = DriverManager.getConnection(
                DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                "SELECT user_id FROM users WHERE username = ? AND password = ?")) {
            
            // in a real application, password should be hashed
            String hashedPassword = hashPassword(password);
            
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
        
        return -1; // authentication failed
    }
    
    // hash a password (same as in JdbcDao)
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            
            // converting byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    public static void main(String[] args) {
        GroceryServer server = new GroceryServer();
        server.start();
    }
}