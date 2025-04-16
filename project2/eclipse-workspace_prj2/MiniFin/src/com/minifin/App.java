package com.minifin;

import com.minifin.database.DatabaseManager;
import com.minifin.model.User;
import com.minifin.view.MainLayout;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

public class App extends Application {
    private DatabaseManager dbManager;
    private User currentUser;

    @Override
    public void init() {
        // Initialize database
        dbManager = DatabaseManager.getInstance();
        
        // Initialize default user (for demo purposes)
        try {
            currentUser = dbManager.getUserByUsername("demo");
            if (currentUser == null) {
                currentUser = dbManager.createUser("demo", "Demo@123", "demo@example.com");
                
                // Create default categories
                dbManager.createCategory("Salary", "Income", currentUser.getId());
                dbManager.createCategory("Freelance", "Income", currentUser.getId());
                dbManager.createCategory("Investments", "Income", currentUser.getId());
                dbManager.createCategory("Other Income", "Income", currentUser.getId());
                
                dbManager.createCategory("Housing", "Expense", currentUser.getId());
                dbManager.createCategory("Utilities", "Expense", currentUser.getId());
                dbManager.createCategory("Groceries", "Expense", currentUser.getId());
                dbManager.createCategory("Transportation", "Expense", currentUser.getId());
                dbManager.createCategory("Entertainment", "Expense", currentUser.getId());
                dbManager.createCategory("Healthcare", "Expense", currentUser.getId());
                dbManager.createCategory("Dining Out", "Expense", currentUser.getId());
                dbManager.createCategory("Shopping", "Expense", currentUser.getId());
                dbManager.createCategory("Personal Care", "Expense", currentUser.getId());
                dbManager.createCategory("Education", "Expense", currentUser.getId());
                dbManager.createCategory("Other Expenses", "Expense", currentUser.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error initializing user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        MainLayout mainLayout = new MainLayout(currentUser);
        
        // Set up the scene
        Scene scene = new Scene(mainLayout, 1200, 800);
        
        // Configure stage
        primaryStage.setTitle("MiniFin - Personal Finance Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Clean up resources
        if (dbManager != null) {
            dbManager.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}