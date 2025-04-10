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

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private GroceryServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private int userId;
    
    public ClientHandler(Socket socket, GroceryServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.username = "Unknown";
        this.userId = -1;
    }
    
    @Override
    public void run() {
        try {
            // setting up input and output streams
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            // handling client authentication
            if (authenticateClient()) {
                // client authenticated, register with server
                server.registerClient(username, this);
                
                // processing client requests
                handleClientRequests();
            }
            
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            // cleanup when client disconnects
            closeConnection();
        }
    }
    
    private boolean authenticateClient() {
        try {
            // sending authentication request to client
            out.println("AUTH_REQUEST");
            
            // reading credentials from client
            String response = in.readLine();
            if (response == null || !response.startsWith("AUTH:")) {
                out.println("AUTH_FAILED:Invalid authentication format");
                return false;
            }
            
            // parsing username and password
            String[] credentials = response.substring(5).split(":");
            if (credentials.length != 2) {
                out.println("AUTH_FAILED:Invalid credentials format");
                return false;
            }
            
            this.username = credentials[0];
            String password = credentials[1];
            
            // authenticating with database
            this.userId = server.authenticateUser(username, password);
            
            if (userId > 0) {
                out.println("AUTH_SUCCESS:" + userId);
                System.out.println("Client authenticated: " + username + " (ID: " + userId + ")");
                return true;
            } else {
                out.println("AUTH_FAILED:Invalid username or password");
                return false;
            }
            
        } catch (IOException e) {
            System.err.println("Authentication error: " + e.getMessage());
            return false;
        }
    }
    
    private void handleClientRequests() throws IOException {
        String inputLine;
        
        // continuous processing of client requests
        while ((inputLine = in.readLine()) != null) {
            // processing different request types
            if (inputLine.equals("DISCONNECT")) {
                break;
            } else if (inputLine.startsWith("MESSAGE:")) {
                String message = inputLine.substring(8);
                server.broadcastMessage(username, message);
            } else if (inputLine.startsWith("CART_ACTION:")) {
                processCartAction(inputLine.substring(12));
            }
        }
    }
    
    private void processCartAction(String action) {
        // cart operations will be delegated to JdbcDao in an actual implementation
        // here we just log the actions for demonstration
        System.out.println("User " + username + " performed cart action: " + action);
        
        // send acknowledgment back to client
        out.println("CART_RESPONSE:Action processed: " + action);
    }
    
    public void sendMessage(String message) {
        if (out != null) {
            out.println("MESSAGE:" + message);
        }
    }
    
    private void closeConnection() {
        try {
            if (username != null && !username.equals("Unknown")) {
                server.removeClient(username);
            }
            
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            
            System.out.println("Connection closed with client: " + username);
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}