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

import com.groceryapp.controller.CartController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class NetworkLoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button exitButton;
    @FXML private javafx.scene.control.Label statusLabel;
    @FXML private javafx.scene.control.Label connectionStatusLabel;
    
    private GroceryClient client;
    
    @FXML
    public void initialize() {
        // initializing connection status
        updateConnectionStatus(false);
        
        // setting up login button
        loginButton.setOnAction(event -> handleLogin(event));
        
        // setting up exit button
        exitButton.setOnAction(event -> {
            if (client != null) {
                client.disconnect();
            }
            Platform.exit();
        });
    }
    
    public void setClient(GroceryClient client) {
        this.client = client;
    }
    
    private void handleLogin(ActionEvent event) {
        Window owner = loginButton.getScene().getWindow();
        
        // validating input
        if (usernameField.getText().isEmpty()) {
            showAlert(AlertType.ERROR, owner, "Login Error", "Please enter your username");
            return;
        }
        
        if (passwordField.getText().isEmpty()) {
            showAlert(AlertType.ERROR, owner, "Login Error", "Please enter your password");
            return;
        }
        
        // connecting to server if not already connected
        if (!client.isConnected()) {
            statusLabel.setText("Connecting to server...");
            boolean connected = client.connect();
            updateConnectionStatus(connected);
            
            if (!connected) {
                statusLabel.setText("Failed to connect to server");
                return;
            }
        }
        
        // performing login
        statusLabel.setText("Logging in...");
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (client.login(username, password)) {
            // login successful
            showAlert(AlertType.INFORMATION, owner, "Login Successful", 
                "Connected to Grocery Server");
            
            // open grocery application
            openGroceryApp();
        } else {
            // login failed
            statusLabel.setText("Login failed. Invalid username or password");
            passwordField.clear();
        }
    }
    
    private void openGroceryApp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groceryapp/view/Grocery.fxml"));
            Parent groceryRoot = loader.load();
            
            CartController controller = loader.getController();
            controller.initData(client.getUserId());
            
            // setting up networked cart controller
            if (controller instanceof NetworkCartController) {
                ((NetworkCartController) controller).setClient(client);
            }
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("Grocery Store - Network Client");
            Scene scene = new Scene(groceryRoot);
            stage.setScene(scene);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, loginButton.getScene().getWindow(), 
                "Navigation Error", "Could not open grocery application: " + e.getMessage());
        }
    }
    
    private void updateConnectionStatus(boolean connected) {
        if (connected) {
            connectionStatusLabel.setText("Connected to server");
            connectionStatusLabel.setStyle("-fx-text-fill: green;");
        } else {
            connectionStatusLabel.setText("Not connected");
            connectionStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void showAlert(AlertType alertType, Window owner, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }
}