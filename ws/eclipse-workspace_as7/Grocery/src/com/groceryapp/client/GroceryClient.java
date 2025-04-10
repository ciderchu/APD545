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

package com.groceryapp.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;

/**
 * Main class for the Grocery Client application.
 * This client connects to the GroceryServer and provides a UI for the shopping cart application.
 */
public class GroceryClient extends Application {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientListener listener;
    private Thread listenerThread;
    
    private boolean connected = false;
    private int userId = -1;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // loading login screen first
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groceryapp/view/NetworkLogin.fxml"));
        Scene scene = new Scene(loader.load());
        
        // setting up the controller with client reference
        NetworkLoginController controller = loader.getController();
        controller.setClient(this);
        
        primaryStage.setTitle("Grocery Store Network Login");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            disconnect();
            Platform.exit();
        });
        primaryStage.show();
    }
    
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // starting the listener thread for server messages
            listener = new ClientListener();
            listenerThread = new Thread(listener);
            listenerThread.setDaemon(true);
            listenerThread.start();
            
            connected = true;
            return true;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            return false;
        }
    }
    
    public boolean login(String username, String password) {
        if (!connected && !connect()) {
            return false;
        }
        
        try {
            // waiting for authentication request from server
            String response = in.readLine();
            if (!response.equals("AUTH_REQUEST")) {
                System.err.println("Unexpected server response: " + response);
                return false;
            }
            
            // sending credentials to server
            out.println("AUTH:" + username + ":" + password);
            
            // getting authentication response
            response = in.readLine();
            if (response.startsWith("AUTH_SUCCESS:")) {
                userId = Integer.parseInt(response.substring(13));
                System.out.println("Login successful. User ID: " + userId);
                return true;
            } else {
                String errorMessage = response.contains(":") ? 
                    response.substring(response.indexOf(":") + 1) : "Authentication failed";
                System.err.println("Login failed: " + errorMessage);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error during login: " + e.getMessage());
            return false;
        }
    }
    
    public void disconnect() {
        if (connected) {
            try {
                if (out != null) {
                    out.println("DISCONNECT");
                }
                
                if (listenerThread != null) {
                    listener.stop();
                    listenerThread.interrupt();
                }
                
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
                
                connected = false;
                System.out.println("Disconnected from server");
            } catch (IOException e) {
                System.err.println("Error disconnecting: " + e.getMessage());
            }
        }
    }
    
    public void sendCartAction(String action) {
        if (connected && out != null) {
            out.println("CART_ACTION:" + action);
        }
    }
    
    public int getUserId() {
        return userId;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    // Inner class to handle messages from the server
    private class ClientListener implements Runnable {
        private volatile boolean running = true;
        
        @Override
        public void run() {
            try {
                String message;
                while (running && (message = in.readLine()) != null) {
                    if (message.startsWith("MESSAGE:")) {
                        String content = message.substring(8);
                        System.out.println("Server message: " + content);
                        
                        // process server messages here
                        // for real implementation, dispatch to appropriate controller
                    } else if (message.startsWith("CART_RESPONSE:")) {
                        String response = message.substring(14);
                        System.out.println("Cart response: " + response);
                        
                        // process cart-specific responses
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error reading from server: " + e.getMessage());
                }
            }
        }
        
        public void stop() {
            running = false;
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}