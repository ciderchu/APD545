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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * GUI launcher for the GroceryServer application.
 * Provides controls to start/stop the server and displays server logs.
 */
public class ServerLauncher extends Application {
    
    private TextArea logArea;
    private Button startButton;
    private Button stopButton;
    private Label statusLabel;
    
    private GroceryServer server;
    private Thread serverThread;
    private AtomicBoolean serverRunning = new AtomicBoolean(false);
    
    @Override
    public void start(Stage primaryStage) {
        // creating the UI components
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // header section
        Label titleLabel = new Label("Grocery Store Server");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        
        statusLabel = new Label("Server Status: STOPPED");
        statusLabel.setStyle("-fx-text-fill: red;");
        
        VBox headerBox = new VBox(10, titleLabel, statusLabel);
        
        // control buttons
        startButton = new Button("Start Server");
        startButton.setOnAction(e -> startServer());
        startButton.setPrefWidth(120);
        
        stopButton = new Button("Stop Server");
        stopButton.setOnAction(e -> stopServer());
        stopButton.setPrefWidth(120);
        stopButton.setDisable(true);
        
        HBox buttonBox = new HBox(10, startButton, stopButton);
        
        VBox topSection = new VBox(10, headerBox, buttonBox);
        root.setTop(topSection);
        
        // log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(400);
        
        ScrollPane scrollPane = new ScrollPane(logArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        root.setCenter(scrollPane);
        
        // redirecting System.out to TextArea
        redirectSystemOutToTextArea();
        
        // setting up the scene
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setTitle("Grocery Server Launcher");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            stopServer();
            Platform.exit();
        });
        
        primaryStage.show();
    }
    
    private void startServer() {
        if (!serverRunning.get()) {
            // updating UI controls
            startButton.setDisable(true);
            stopButton.setDisable(false);
            statusLabel.setText("Server Status: STARTING...");
            statusLabel.setStyle("-fx-text-fill: orange;");
            
            // clearing log area
            logArea.clear();
            
            // creating server instance
            server = new GroceryServer();
            
            // starting server in a separate thread
            serverThread = new Thread(() -> {
                serverRunning.set(true);
                Platform.runLater(() -> {
                    statusLabel.setText("Server Status: RUNNING");
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
                
                server.start(); // blocking call
            });
            serverThread.setDaemon(true);
            serverThread.start();
            
            System.out.println("Server starting on a separate thread...");
        }
    }
    
    private void stopServer() {
        if (serverRunning.get() && server != null) {
            // updating UI controls
            statusLabel.setText("Server Status: STOPPING...");
            statusLabel.setStyle("-fx-text-fill: orange;");
            
            // stopping the server
            new Thread(() -> {
                server.stop();
                serverRunning.set(false);
                Platform.runLater(() -> {
                    startButton.setDisable(false);
                    stopButton.setDisable(true);
                    statusLabel.setText("Server Status: STOPPED");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    System.out.println("Server stopped.");
                });
            }).start();
        }
    }
    
    private void redirectSystemOutToTextArea() {
        // creating a custom PrintStream that writes to the TextArea
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // TextArea append needs to run on JavaFX thread
                Platform.runLater(() -> {
                    logArea.appendText(String.valueOf((char) b));
                    // auto-scroll to bottom
                    logArea.setScrollTop(Double.MAX_VALUE);
                });
            }
            
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                Platform.runLater(() -> {
                    logArea.appendText(new String(b, off, len));
                    // auto-scroll to bottom
                    logArea.setScrollTop(Double.MAX_VALUE);
                });
            }
        };
        
        // redirecting System.out to our custom output stream
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
    
    @Override
    public void stop() {
        stopServer();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}